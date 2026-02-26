package com.covercloud.cover.service

import com.covercloud.cover.domain.CoverLike
import com.covercloud.cover.repository.CoverLikeRepository
import com.covercloud.cover.repository.CoverRepository
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields

@Service
class LikeService(
    private val redisTemplate: StringRedisTemplate,
    private val likeScript: RedisScript<Long>,
    private val unlikeScript: RedisScript<Long>,
    private val coverRepository: CoverRepository,
    private val coverLikeRepository: CoverLikeRepository
) {
    
    private val logger = LoggerFactory.getLogger(LikeService::class.java)
    
    companion object {
        private const val LIKE_SET_PREFIX = "cover:likes:"
        private const val LIKE_COUNT_PREFIX = "cover:likeCount:"
        private const val DIRTY_SET_KEY = "cover:dirty"
    }
    private fun updateTrendingScore(coverId: Long, delta: Double) {
        val now = LocalDateTime.now()
        val coverIdStr = coverId.toString()

        val trendingConfigs = listOf(
            "trending:daily:${now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}" to Duration.ofDays(2),
            "trending:weekly:${now.get(IsoFields.WEEK_BASED_YEAR)}-W${now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}" to Duration.ofDays(14),
            "trending:monthly:${now.format(DateTimeFormatter.ofPattern("yyyy-MM"))}" to Duration.ofDays(60)
        )

        trendingConfigs.forEach { (key, ttl) ->
            // 스코어 업데이트
            redisTemplate.opsForZSet().incrementScore(key, coverIdStr, delta)
            // TTL 설정: 이미 설정되어 있어도 갱신해주거나,
            // 랭킹 키가 처음 생성되었을 때를 위해 유지합니다.
            redisTemplate.expire(key, ttl)
        }
    }
    private fun likeSetKey(coverId: Long) = "$LIKE_SET_PREFIX$coverId"
    private fun likeCountKey(coverId: Long) = "$LIKE_COUNT_PREFIX$coverId"
    
    fun like(coverId: Long, userId: Long): Boolean {
        try {
            val result = redisTemplate.execute(
                likeScript,
                listOf(likeSetKey(coverId), likeCountKey(coverId)),
                userId.toString()
            )
            
            if (result != null && result > 0) {
                // Mark as dirty for batch sync
                redisTemplate.opsForSet().add(DIRTY_SET_KEY, coverId.toString())
                updateTrendingScore(coverId, 1.0)
                logger.info("User $userId liked cover $coverId. New count: $result")
                return true
            }

            logger.info("User $userId already liked cover $coverId")
            return false
        } catch (e: Exception) {
            logger.error("Failed to like cover $coverId by user $userId", e)
            return false
        }
    }
    
    fun unlike(coverId: Long, userId: Long): Boolean {
        try {
            val result = redisTemplate.execute(
                unlikeScript,
                listOf(likeSetKey(coverId), likeCountKey(coverId)),
                userId.toString()
            )
            
            if (result != null && result >= 0) {
                // Mark as dirty for batch sync
                redisTemplate.opsForSet().add(DIRTY_SET_KEY, coverId.toString())
                updateTrendingScore(coverId, -1.0)
                logger.info("User $userId unliked cover $coverId. New count: $result")
                return true
            }
            logger.info("User $userId had not liked cover $coverId")
            return false
        } catch (e: Exception) {
            logger.error("Failed to unlike cover $coverId by user $userId", e)
            return false
        }
    }

    fun getLikeCount(coverId: Long): Long {
        val redisKey = likeCountKey(coverId)
        val count = redisTemplate.opsForValue().get(redisKey)

        // 1. Redis에 숫자가 있으면 바로 반환 (가장 빠름)
        if (count != null) return count.toLongOrNull() ?: 0L

        // 2. Redis에 없으면 DB에서 "진짜 행 개수"를 세어옴 (컬럼값 X)
        val actualCount = coverLikeRepository.countByCoverId(coverId)

        // 3. Redis에 이 값을 채워줌 (다음 조회를 위해)
        redisTemplate.opsForValue().set(redisKey, actualCount.toString())

        return actualCount
    }
    
    fun hasLiked(coverId: Long, userId: Long): Boolean {
        return try {
            redisTemplate.opsForSet().isMember(likeSetKey(coverId), userId.toString()) ?: false
        } catch (e: Exception) {
            logger.error("Failed to check if user $userId liked cover $coverId", e)
            false
        }
    }
    
    fun initializeLikeCount(coverId: Long): Long {
        try {
            val currentCount = redisTemplate.opsForValue().get(likeCountKey(coverId))
            if (currentCount == null) {
                // Initialize from DB
                val cover = coverRepository.findByIdOrNull(coverId)
                val dbCount = cover?.likeCount ?: 0L
                redisTemplate.opsForValue().set(likeCountKey(coverId), dbCount.toString())
                logger.info("Initialized like count for cover $coverId from DB: $dbCount")
                return dbCount
            }
            return currentCount.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            logger.error("Failed to initialize like count for cover $coverId", e)
            return 0L
        }
    }

    @Scheduled(fixedDelayString = "10000")
    @Transactional
    fun syncLikesToDatabase() {
        try {
            val dirtyCoverIds = redisTemplate.opsForSet().members(DIRTY_SET_KEY) ?: return
            if (dirtyCoverIds.isEmpty()) return

            logger.info("🔄 Syncing ${dirtyCoverIds.size} covers: $dirtyCoverIds")

            for (coverIdStr in dirtyCoverIds) {
                val coverId = coverIdStr.toLongOrNull() ?: continue

                // 1. Redis에서 데이터 확보
                val redisLikedUsers = redisTemplate.opsForSet().members(likeSetKey(coverId)) ?: emptySet()

                // 🚨 [방어 로직] Redis 명단이 아예 비어있다면, 시스템 오류일 수 있으므로
                // DB를 함부로 지우지 않고 건너뛰거나 로그를 남깁니다. (Redis가 Master이므로 신중해야 함)
                if (redisLikedUsers.isEmpty()) {
                    // 정말 좋아요가 0개가 된 건지, Redis 키가 증발한 건지 판단이 필요합니다.
                    // 일단 여기서는 '개수'가 0인 경우에만 진행하도록 하거나 안전하게 로깅만 합니다.
                    logger.warn("⚠️ Redis like set for $coverId is empty. Skipping delete for safety.")
                    // count만 0으로 업데이트하고 continue 하거나 로직을 분기하세요.
                }

                // 2. DB 현황 파악
                val dbLikes = coverLikeRepository.findAllByCoverId(coverId)
                val dbUserIds = dbLikes.map { it.userId.toString() }.toSet()

                // 3. 추가 로직 (Redis에만 있는 유저)
                (redisLikedUsers - dbUserIds).forEach { userIdStr ->
                    val userId = userIdStr.toLongOrNull() ?: return@forEach
                    // 중복 insert 방지를 위해 한 번 더 체크 (Unique 제약조건 에러 방지)
                    if (!coverLikeRepository.existsByCoverIdAndUserId(coverId, userId)) {
                        coverLikeRepository.save(CoverLike(cover = coverRepository.getReferenceById(coverId), userId = userId))
                        logger.info("➕ Added: Cover $coverId, User $userId")
                    }
                }

                // 4. 삭제 로직 (DB에만 있는 유저)
                (dbUserIds - redisLikedUsers).forEach { userIdStr ->
                    val userId = userIdStr.toLongOrNull() ?: return@forEach
                    coverLikeRepository.deleteByCoverIdAndUserId(coverId, userId)
                    logger.info("➖ Removed: Cover $coverId, User $userId")
                }

                // 5. 최종 개수 업데이트 (Redis 숫자가 진실이므로 그대로 반영)
                val finalCount = redisLikedUsers.size.toLong()
                coverRepository.updateLikeCount(coverId, finalCount)

                // 6. 성공 시 Dirty Set에서 제거
                redisTemplate.opsForSet().remove(DIRTY_SET_KEY, coverIdStr)
            }
            logger.info("🎉 Sync completed successfully")
        } catch (e: Exception) {
            logger.error("💥 Sync failed", e)
        }
    }
}