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
        return try {
            val count = redisTemplate.opsForValue().get(likeCountKey(coverId))
            count?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            logger.error("Failed to get like count for cover $coverId", e)
            // Fallback to DB
            coverRepository.findByIdOrNull(coverId)?.likeCount ?: 0L
        }
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
    
    @Scheduled(fixedDelayString = "10000") // Every 10 seconds for testing
    @Transactional
    fun syncLikesToDatabase() {
        try {
            val dirtyCoverIds = redisTemplate.opsForSet().members(DIRTY_SET_KEY) ?: return
            
            if (dirtyCoverIds.isEmpty()) {
                logger.debug("No dirty covers to sync")
                return
            }
            
            logger.info("🔄 Syncing ${dirtyCoverIds.size} cover like counts to database: $dirtyCoverIds")
            
            for (coverIdStr in dirtyCoverIds) {
                try {
                    val coverId = coverIdStr.toLongOrNull() ?: continue
                    
                    // Get Redis data
                    val redisLikedUsers = redisTemplate.opsForSet().members(likeSetKey(coverId)) ?: emptySet()
                    val redisCount = getLikeCount(coverId)
                    
                    logger.info("📊 Processing cover $coverId: Redis count = $redisCount, users = $redisLikedUsers")
                    
                    val cover = coverRepository.findByIdOrNull(coverId)
                    if (cover != null) {
                        // 1. Update cover.like_count
                        val oldCount = cover.likeCount
                        cover.likeCount = redisCount
                        coverRepository.save(cover)
                        
                        // 2. Sync cover_like table
                        val dbLikes = coverLikeRepository.findAllByCoverId(coverId)
                        val dbUserIds = dbLikes.map { it.userId.toString() }.toSet()
                        
                        // Add new likes
                        val usersToAdd = redisLikedUsers - dbUserIds
                        usersToAdd.forEach { userIdStr ->
                            val userId = userIdStr.toLongOrNull()
                            if (userId != null) {
                                try {
                                    coverLikeRepository.save(CoverLike(cover = cover, userId = userId))
                                    logger.info("➕ Added like: cover $coverId, user $userId")
                                } catch (e: Exception) {
                                    logger.warn("Failed to add like for user $userId on cover $coverId: ${e.message}")
                                }
                            }
                        }
                        
                        // Remove old likes
                        val usersToRemove = dbUserIds - redisLikedUsers
                        usersToRemove.forEach { userIdStr ->
                            val userId = userIdStr.toLongOrNull()
                            if (userId != null) {
                                coverLikeRepository.deleteByCoverIdAndUserId(coverId, userId)
                                logger.info("➖ Removed like: cover $coverId, user $userId")
                            }
                        }
                        
                        // Remove from dirty set after successful sync
                        redisTemplate.opsForSet().remove(DIRTY_SET_KEY, coverIdStr)
                        logger.info("✅ Synced cover $coverId: count $oldCount → $redisCount, likes table updated")
                    } else {
                        logger.warn("⚠️ Cover $coverId not found in database")
                        // Remove invalid cover ID from dirty set
                        redisTemplate.opsForSet().remove(DIRTY_SET_KEY, coverIdStr)
                    }
                } catch (e: Exception) {
                    logger.error("❌ Failed to sync likes for cover $coverIdStr", e)
                }
            }
            
            logger.info("🎉 Like count sync completed")
        } catch (e: Exception) {
            logger.error("💥 Failed to sync likes to database", e)
        }
    }
}