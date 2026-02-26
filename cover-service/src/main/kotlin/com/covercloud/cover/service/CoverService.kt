package com.covercloud.cover.service

import com.covercloud.cover.controller.dto.SearchSort
import com.covercloud.cover.service.dto.CoverResponse
import com.covercloud.cover.service.dto.CoverListResponse
import com.covercloud.cover.service.dto.CreateServiceCoverRequest
import com.covercloud.cover.service.dto.PageResponse
import com.covercloud.cover.service.dto.TrendingCoverResponse
import com.covercloud.cover.domain.Cover
import com.covercloud.cover.domain.CoverTag
import com.covercloud.cover.domain.Tag
import com.covercloud.cover.domain.TrendingPeriod
import com.covercloud.cover.domain.CoverGenre
//import com.covercloud.cover.global.security.SecurityUtil
import com.covercloud.cover.infrastructure.dto.CreateMusicRequest
import com.covercloud.cover.infrastructure.dto.UserProfileDto
import com.covercloud.cover.infrastructure.feign.MusicClient
import com.covercloud.cover.infrastructure.feign.UserClient
import com.covercloud.cover.repository.CoverRepository
import com.covercloud.cover.repository.CoverTagRepository
import com.covercloud.cover.repository.TagRepository
import com.covercloud.cover.repository.CoverLikeRepository
import jakarta.transaction.Transactional
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.DayOfWeek
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters

@Service
class CoverService(
    private val coverRepository: CoverRepository,
    private val tagRepository: TagRepository,
    private val coverTagRepository: CoverTagRepository,
    private val musicClient: MusicClient,
    private val userClient: UserClient,
    private val coverLikeRepository: CoverLikeRepository,
    private val redisTemplate: StringRedisTemplate,
    private val likeService: LikeService
    ) {
    private fun convertToGenreEnums(genres: List<String>?): List<CoverGenre>? {
        return if (genres.isNullOrEmpty()) null
        else genres.mapNotNull { g ->
            runCatching {
                CoverGenre.valueOf(g.uppercase().replace("-", "_"))
            }.getOrNull()
        }.distinct().ifEmpty { null }
    }

    @Transactional
    fun uploadCover(request: CreateServiceCoverRequest, userId: Long): CoverResponse {
        val musicResult = musicClient.saveMusic(
            CreateMusicRequest(
                title = request.originalTitle,
                artist = request.originalArtist,
                originalCoverImageUrl = request.originalCoverImageUrl?: ""
            )
        )
        val cover = Cover(
            musicId = musicResult.id,
            userId = userId,
            link = request.videoUrl,
            coverArtist = request.coverArtist,
            coverGenre = request.genre,
            coverTitle = request.title
        )

        val savedCover = coverRepository.save(cover)

        request.tags?.forEach { tagName ->
            val tag = tagRepository.findByName(tagName)
                ?: tagRepository.save(Tag(name = tagName))

            coverTagRepository.save(CoverTag(cover, tag))
        }

        return CoverResponse(
            coverId = savedCover.id!!,
            musicId = savedCover.musicId,
            coverTitle = savedCover.coverTitle,
            coverArtist = savedCover.coverArtist,
            coverGenre = savedCover.coverGenre,
            tags = request.tags,
            link = savedCover.link,
            originalCoverImageUrl = request.originalCoverImageUrl
        )
    }

    @Transactional
    fun updateCover(
        id: Long,
        request: CreateServiceCoverRequest,
    ): CoverResponse {
        val cover = coverRepository.findByIdOrNull(id) ?: throw NotFoundException()


        if (cover.musicId == null) {
            val musicResult = musicClient.saveMusic(
                CreateMusicRequest(
                    title = request.originalTitle,
                    artist = request.originalArtist,
                    originalCoverImageUrl = request.originalCoverImageUrl ?: ""
                )
            )
            cover.musicId = musicResult.id
        }

        request.title?.let { cover.coverTitle = it }
        request.coverArtist?.let { cover.coverArtist = it }
        request.genre?.let { cover.coverGenre = it }
        request.videoUrl?.let { cover.link = it }

        request.tags?.let { newTagList ->
            val existingTags = coverTagRepository.findAllByCoverId(cover.id!!)
                .associateBy { it.tag.name }

            val newTagNames = newTagList.toSet()

            // 기존 태그 중 삭제할 태그
            existingTags.keys
                .filter { it !in newTagNames }
                .forEach { tagName ->
                    coverTagRepository.delete(existingTags[tagName]!!)
                }

            // 새로 추가할 태그
            newTagNames
                .filter { it !in existingTags.keys }
                .forEach { tagName ->
                    val tag = tagRepository.findByName(tagName) ?: tagRepository.save(Tag(name = tagName))
                    coverTagRepository.save(CoverTag(cover, tag))
                }
        }

        val responseTags = request.tags ?: coverTagRepository.findAllByCoverId(cover.id!!)
            .map { it.tag.name }

        return CoverResponse(
            coverId = cover.id!!,
            musicId = cover.musicId,
            coverTitle = cover.coverTitle,
            coverArtist = cover.coverArtist,
            coverGenre = cover.coverGenre,
            tags = responseTags,
            link = cover.link,
            originalCoverImageUrl = request.originalCoverImageUrl
        )
    }

    @Transactional
    fun deleteCover(coverId: Long) {
        val cover = coverRepository.findByIdOrNull(coverId) ?: throw NotFoundException()
        coverLikeRepository.deleteAllByCoverId(coverId)
        coverTagRepository.deleteAllByCoverId(cover.id!!)
        coverRepository.delete(cover)
    }

    fun getCovers(
        period: TrendingPeriod?,
        page: Int = 0,
        size: Int = 20,
        genres: List<String>? = null,
        userId: Long? = null
    ): PageResponse<CoverListResponse> {
        val genreEnums = convertToGenreEnums(genres)
        val now = LocalDateTime.now()

        // 1. Redis에서 인기 ID 리스트 가져오기 (없으면 빈 리스트)
        val trendingIds = if (period != null) {
            val redisKey = generateTrendingKey(period)
            val topIds = redisTemplate.opsForZSet().reverseRange(redisKey, 0, 999) ?: emptySet()
            topIds.mapNotNull { it.toLongOrNull() }
        } else {
            emptyList()
        }

        // 2. 해당 기간에 생성된 "모든" 데이터 조회 (최신순 기본)
        // period가 MONTHLY라면 한 달 전부터 생성된 데이터를 가져옵니다.
        val startDate = when (period) {
            TrendingPeriod.DAILY -> now.minusDays(1)
            TrendingPeriod.WEEKLY -> now.minusWeeks(1)
            TrendingPeriod.MONTHLY -> now.minusMonths(1)
            else -> null // 전체 기간
        }

        // DB에서 조건에 맞는 모든 데이터를 일단 최신순으로 가져옵니다.
        val allCovers = coverRepository.findAllByFilters(startDate, genreEnums)

        // 3. 커스텀 정렬 적용
        // 1순위: trendingIds에 포함된 순서대로
        // 2순위: 그 외 데이터는 최신순(createdAt DESC)
        val sortedContent = allCovers.sortedWith(compareByDescending<Cover> { cover ->
            val index = trendingIds.indexOf(cover.id)
            if (index != -1) trendingIds.size - index else -1
            // 랭킹에 있으면 높은 점수, 없으면 -1
        }.thenByDescending { it.createdAt })

        // 4. 페이징 처리
        val totalElements = sortedContent.size
        val start = page * size
        val end = (start + size).coerceAtMost(totalElements)

        if (start >= totalElements) {
            // PageResponse.empty() 대신 직접 생성
            return PageResponse(
                content = emptyList(),
                pageNumber = page,
                pageSize = size,
                totalElements = totalElements.toLong(),
                totalPages = (totalElements + size - 1) / size,
                isFirst = page == 0,
                isLast = true
            )
        }

        val pagedList = sortedContent.subList(start, end)

        // 5. 응답 변환 (기존 로직)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
// 응답 변환 로직
        val content = pagedList.map { cover ->
            // 🔥 중요: DB 엔티티의 값은 무시하고 Redis에서 실시간 숫자를 가져옵니다.
            val realTimeCount = likeService.getLikeCount(cover.id!!)

            // cover 객체의 필드를 Redis 값으로 덮어씌웁니다.
            cover.likeCount = realTimeCount

            buildCoverListResponse(
                cover = cover,
                dateFormatter = formatter,
                includeMusic = false,
                userId = userId
            )
        }
        return PageResponse(
            content = content,
            pageNumber = page,
            pageSize = size,
            totalElements = totalElements.toLong(),
            totalPages = (totalElements + size - 1) / size,
            isFirst = page == 0,
            isLast = end == totalElements
        )
    }

    // 헬퍼: Redis 키 생성
    private fun generateTrendingKey(period: TrendingPeriod): String {
        val now = LocalDateTime.now()
        return when (period) {
            TrendingPeriod.DAILY -> "trending:daily:${now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}"
            TrendingPeriod.WEEKLY -> "trending:weekly:${now.get(IsoFields.WEEK_BASED_YEAR)}-W${now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}"
            TrendingPeriod.MONTHLY -> "trending:monthly:${now.format(DateTimeFormatter.ofPattern("yyyy-MM"))}"
        }
    }

    // 헬퍼: 공통 응답 변환
    private fun toPageResponse(coverPage: Page<Cover>, userId: Long?): PageResponse<CoverListResponse> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val content = coverPage.content.map { cover ->
            buildCoverListResponse(cover, formatter, false, userId)
        }
        return PageResponse(
            content = content,
            pageNumber = coverPage.number,
            pageSize = coverPage.size,
            totalElements = coverPage.totalElements,
            totalPages = coverPage.totalPages,
            isFirst = coverPage.isFirst,
            isLast = coverPage.isLast
        )
    }

    @Transactional
    fun getCoverById(coverId: Long, userId: Long? = null): CoverListResponse {
        val cover = coverRepository.findByIdOrNull(coverId)
            ?: throw IllegalArgumentException("Cover not found with id: $coverId")
        
        // 조회수 증가
        cover.viewCount++
        coverRepository.save(cover)
        
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        // 원본 음악 정보를 포함하여 응답 생성
        return buildCoverListResponse(cover, dateFormatter, includeMusic = true, userId = userId)
    }


    fun getTrendingCovers(
        period: TrendingPeriod?,
        page: Int = 0,
        size: Int = 20,
        genres: List<String>? = null,
        userId: Long? = null
    ): PageResponse<TrendingCoverResponse> {
        // period가 null이면 전체 기간, 있으면 해당 기간 시작 시점 계산
        val startDate = when (period) {
            TrendingPeriod.DAILY -> LocalDateTime.now().toLocalDate().atStartOfDay()
            TrendingPeriod.WEEKLY -> LocalDateTime.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toLocalDate().atStartOfDay()
            TrendingPeriod.MONTHLY -> LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay()
            null -> LocalDateTime.of(2000, 1, 1, 0, 0)  // 전체 기간
        }

        // 기간 내 좋아요 수 조회
        val periodLikeCounts = coverLikeRepository.countLikesByPeriod(startDate)
            .associate { array -> (array[0] as Long) to (array[1] as Long) }

        // 모든 커버 가져와서 증가량 계산 (좋아요 없어도 포함)
        var allCovers = coverRepository.findAll()

        // 장르 필터링: 여러 장르가 전달되면 OR 조건으로 포함
        if (!genres.isNullOrEmpty()) {
            val coverGenres = genres.mapNotNull { g ->
                try {
                    CoverGenre.valueOf(g.uppercase().replace("-", "_"))
                } catch (_: Exception) {
                    null
                }
            }.toSet()

            if (coverGenres.isNotEmpty()) {
                allCovers = allCovers.filter { it.coverGenre in coverGenres }
            }
        }

        val trendingCovers = allCovers
            .map { cover ->
                val periodLikes = periodLikeCounts[cover.id!!] ?: 0L
                Triple(cover, cover.likeCount - periodLikes, periodLikes)
            }
            .sortedWith(compareByDescending<Triple<Cover, Long, Long>> { it.third }
                .thenByDescending { it.first.createdAt })

        // 페이징 처리
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, trendingCovers.size)
        val pagedCovers = if (startIndex < trendingCovers.size) {
            trendingCovers.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        val content = pagedCovers.map { (cover, previousLikeCount, periodLikes) ->
            val tags = coverTagRepository.findAllByCoverId(cover.id!!)
                .map { it.tag.name }

            val isLiked = if (userId != null) {
                coverLikeRepository.existsByCoverIdAndUserId(cover.id!!, userId)
            } else {
                false
            }

            TrendingCoverResponse(
                coverId = cover.id!!,
                musicId = cover.musicId,
                userId = cover.userId,
                coverArtist = cover.coverArtist,
                coverTitle = cover.coverTitle,
                coverGenre = cover.coverGenre,
                link = cover.link,
                currentLikeCount = cover.likeCount,
                previousLikeCount = previousLikeCount,
                likeIncrement = periodLikes,
                viewCount = cover.viewCount,
                commentCount = cover.commentCount,
                tags = tags,
                createdAt = cover.createdAt.format(dateFormatter),
                isLiked = isLiked
            )
        }

        val totalElements = trendingCovers.size.toLong()
        val totalPages = (totalElements + size - 1) / size

        return PageResponse(
            content = content,
            pageNumber = page,
            pageSize = size,
            totalElements = totalElements,
            totalPages = totalPages.toInt(),
            isFirst = page == 0,
            isLast = page >= totalPages - 1
        )
    }


     fun getCoversByUserId(
        userId: Long,
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "createdAt",
        sortDirection: String = "DESC"
    ): PageResponse<CoverListResponse> {
        val sort = if (sortDirection.uppercase() == "ASC") {
            Sort.by(sortBy).ascending()
        } else {
            Sort.by(sortBy).descending()
        }

        val pageable: Pageable = PageRequest.of(page, size, sort)
        val coverPage = coverRepository.findAllByUserId(userId, pageable)

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        val content = coverPage.content.map { cover ->
            buildCoverListResponse(cover, dateFormatter, includeMusic = false, userId = userId)
        }

        return PageResponse(
            content = content,
            pageNumber = coverPage.number,
            pageSize = coverPage.size,
            totalElements = coverPage.totalElements,
            totalPages = coverPage.totalPages,
            isFirst = coverPage.isFirst,
            isLast = coverPage.isLast
        )
    }

    fun searchCoversByTitle(
        title: String,
        page: Int = 0,
        size: Int = 20,
        sortBy: SearchSort,
        userId: Long? = null
    ): PageResponse<CoverListResponse> {

        val pageable = when (sortBy) {
            SearchSort.POPULAR ->
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "likeCount"))

            SearchSort.LATEST ->
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        }

        val coverPage = coverRepository.findByCoverTitleContainingIgnoreCase(title, pageable)

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        val content = coverPage.content.map { cover ->
            buildCoverListResponse(cover, dateFormatter, includeMusic = false, userId = userId)
        }

        return PageResponse(
            content = content,
            pageNumber = coverPage.number,
            pageSize = coverPage.size,
            totalElements = coverPage.totalElements,
            totalPages = coverPage.totalPages,
            isFirst = coverPage.isFirst,
            isLast = coverPage.isLast
        )
    }

    fun searchCoversByTags(
        tags: String,
        page: Int = 0,
        size: Int = 20,
        sortBy: SearchSort,
        userId: Long? = null
    ): PageResponse<CoverListResponse> {
        val pageable = when (sortBy) {
            SearchSort.POPULAR ->
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "likeCount"))

            SearchSort.LATEST ->
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        }

        val coverPage = coverRepository.searchByTags(tags, pageable)

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        val content = coverPage.content.map { cover ->
            buildCoverListResponse(cover, dateFormatter, includeMusic = false, userId = userId)
        }

        return PageResponse(
            content = content,
            pageNumber = coverPage.number,
            pageSize = coverPage.size,
            totalElements = coverPage.totalElements,
            totalPages = coverPage.totalPages,
            isFirst = coverPage.isFirst,
            isLast = coverPage.isLast
        )
    }

    // ✅ 사용자가 댓글을 단 커버들 조회
    fun getCoversByUserComments(
        userId: Long,
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "createdAt",
        sortDirection: String = "DESC"
    ): PageResponse<CoverListResponse> {
        val sort = if (sortDirection.uppercase() == "ASC") {
            Sort.by(sortBy).ascending()
        } else {
            Sort.by(sortBy).descending()
        }

        val pageable: Pageable = PageRequest.of(page, size, sort)
        val coverPage = coverRepository.findCoversByUserComments(userId, pageable)

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        val content = coverPage.content.map { cover ->
            buildCoverListResponse(cover, dateFormatter, includeMusic = false, userId = userId)
        }

        return PageResponse(
            content = content,
            pageNumber = coverPage.number,
            pageSize = coverPage.size,
            totalElements = coverPage.totalElements,
            totalPages = coverPage.totalPages,
            isFirst = coverPage.isFirst,
            isLast = coverPage.isLast
        )
    }

    /**
     * 사용자가 좋아요한 커버곡 조회
     */
    fun getLikedCovers(
        userId: Long,
        page: Int = 0,
        size: Int = 20
    ): PageResponse<CoverListResponse> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))

        // 1. 사용자가 좋아요한 커버 ID 조회
        val likedCoverIds = coverLikeRepository.findAllByUserId(userId)
            .mapNotNull { it.cover?.id }

        if (likedCoverIds.isEmpty()) {
            return PageResponse(
                content = emptyList(),
                pageNumber = page,
                pageSize = size,
                totalElements = 0L,
                totalPages = 0,
                isFirst = true,
                isLast = true
            )
        }

        // 2. 커버 정보 조회
        val covers = coverRepository.findAllById(likedCoverIds)
            .sortedByDescending { it.createdAt }
            .let { allCovers ->
                val startIdx = page * size
                val endIdx = minOf(startIdx + size, allCovers.size)
                if (startIdx >= allCovers.size) emptyList()
                else allCovers.subList(startIdx, endIdx)
            }

        // 3. DTO 매핑
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val content = covers.map { cover ->
            buildCoverListResponse(
                cover = cover,
                dateFormatter = formatter,
                includeMusic = false,
                userId = userId
            )
        }

        val totalElements = likedCoverIds.size.toLong()
        val totalPages = (totalElements + size - 1) / size

        return PageResponse(
            content = content,
            pageNumber = page,
            pageSize = size,
            totalElements = totalElements,
            totalPages = totalPages.toInt(),
            isFirst = page == 0,
            isLast = (page + 1) * size >= totalElements
        )
    }

    private fun buildCoverListResponse(
        cover: Cover,
        dateFormatter: java.time.format.DateTimeFormatter,
        includeMusic: Boolean = false,
        userId: Long? = null
    ): CoverListResponse {
        val tags = coverTagRepository.findAllByCoverId(cover.id!!)
            .map { it.tag.name }

        var originalTitle: String? = null
        var originalArtist: String? = null
        var originalCoverImageUrl: String? = null

        if (includeMusic) {
            try {
                val music = musicClient.getMusic(cover.musicId)
                originalTitle = music.title
                originalArtist = music.artist
                originalCoverImageUrl = music.originalCoverImageUrl
            } catch (_: Exception) {
                // Music 정보 조회 실패 시 무시
            }
        }

        val isLiked = if (userId != null) {
            coverLikeRepository.existsByCoverIdAndUserId(cover.id!!, userId)
        } else {
            false
        }

        // ✅ 사용자 정보 조회 및 삭제된 사용자 처리
        var nickname = "Unknown"
        var profileImage: String? = null
        var isAuthorDeleted = false

        try {
            val userProfiles = userClient.getUsersByIds(listOf(cover.userId))
            val userProfile = userProfiles.data?.firstOrNull()
            if (userProfile?.isDeleted == true) {
                nickname = "익명 사용자"
                profileImage = null
                isAuthorDeleted = true
            } else {
                nickname = userProfile?.nickname ?: "Unknown"
                profileImage = userProfile?.profileImageUrl
            }
        } catch (_: Exception) {
            // 사용자 정보 조회 실패 시 기본값 유지
        }

        return CoverListResponse(
            coverId = cover.id!!,
            musicId = cover.musicId,
            userId = cover.userId,
            nickname = nickname,
            profileImage = profileImage,
            coverArtist = cover.coverArtist,
            coverTitle = cover.coverTitle,
            originalArtist = originalArtist,
            originalTitle = originalTitle,
            originalCoverImageUrl = originalCoverImageUrl,
            coverGenre = cover.coverGenre,
            link = cover.link,
            viewCount = cover.viewCount,
            likeCount = cover.likeCount,
            commentCount = cover.commentCount,
            tags = tags,
            createdAt = cover.createdAt.format(dateFormatter),
            isLiked = isLiked,
            isAuthorDeleted = isAuthorDeleted,
            isReported = cover.isReported,
            reportReason = cover.reportReason,
            reportDescription = cover.reportDescription
        )
    }
}