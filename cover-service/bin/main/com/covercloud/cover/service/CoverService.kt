package com.covercloud.cover.service

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
import com.covercloud.cover.infrastructure.feign.MusicClient
import com.covercloud.cover.repository.CoverRepository
import com.covercloud.cover.repository.CoverTagRepository
import com.covercloud.cover.repository.TagRepository
import com.covercloud.cover.repository.CoverLikeRepository
import jakarta.transaction.Transactional
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

@Service
class CoverService(
    private val coverRepository: CoverRepository,
    private val tagRepository: TagRepository,
    private val coverTagRepository: CoverTagRepository,
    private val musicClient: MusicClient,
    private val coverLikeRepository: CoverLikeRepository,
) {
    @Transactional
    fun uploadCover(request: CreateServiceCoverRequest, userId: Long): CoverResponse {
        val musicResult = musicClient.saveMusic(
            CreateMusicRequest(
                title = request.originalTitle,
                artist = request.originalArtist
            )
        )
        val cover = Cover(
            musicId = musicResult.id,
            userId = userId,
            link = request.videoUrl,
            coverArtist = request.originalArtist,
            coverGenre = request.genre,
            coverTitle = request.title
        )

        val savedCover = coverRepository.save(cover);

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
            link = savedCover.link
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
                    artist = request.originalArtist
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
            link = cover.link
        )
    }

    @Transactional
    fun deleteCover(coverId: Long) {
        val cover = coverRepository.findByIdOrNull(coverId) ?: throw NotFoundException()
        coverTagRepository.deleteAllByCoverId(cover.id!!)
        coverRepository.delete(cover)
    }

    fun getCovers(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "createdAt",
        sortDirection: String = "DESC",
        genre: String? = null
    ): PageResponse<CoverListResponse> {
        val sort = if (sortDirection.uppercase() == "ASC") {
            Sort.by(sortBy).ascending()
        } else {
            Sort.by(sortBy).descending()
        }
        
        val pageable: Pageable = PageRequest.of(page, size, sort)
        val coverPage = if (genre != null) {
            val coverGenre = CoverGenre.valueOf(genre.uppercase().replace("-", "_"))
            coverRepository.findAllByCoverGenre(coverGenre, pageable)
        } else {
            coverRepository.findAll(pageable)
        }
        
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        
        val content = coverPage.content.map { cover ->
            val tags = coverTagRepository.findAllByCoverId(cover.id!!)
                .map { it.tag.name }
            
            CoverListResponse(
                coverId = cover.id!!,
                musicId = cover.musicId,
                userId = cover.userId,
                coverArtist = cover.coverArtist,
                coverTitle = cover.coverTitle,
                coverGenre = cover.coverGenre,
                link = cover.link,
                viewCount = cover.viewCount,
                likeCount = cover.likeCount,
                commentCount = cover.commentCount,
                tags = tags,
                createdAt = cover.createdAt?.format(dateFormatter) ?: ""
            )
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
    fun getCoverById(coverId: Long): CoverListResponse {
        val cover = coverRepository.findByIdOrNull(coverId) 
            ?: throw IllegalArgumentException("Cover not found with id: $coverId")
        
        // 조회수 증가
        cover.viewCount++
        coverRepository.save(cover)
        
        val tags = coverTagRepository.findAllByCoverId(cover.id!!)
            .map { it.tag.name }
        
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        
        return CoverListResponse(
            coverId = cover.id!!,
            musicId = cover.musicId,
            userId = cover.userId,
            coverArtist = cover.coverArtist,
            coverTitle = cover.coverTitle,
            coverGenre = cover.coverGenre,
            link = cover.link,
            viewCount = cover.viewCount,
            likeCount = cover.likeCount,
            commentCount = cover.commentCount,
            tags = tags,
            createdAt = cover.createdAt?.format(dateFormatter) ?: ""
        )
    }

    fun getTrendingCovers(
        period: TrendingPeriod,
        page: Int = 0,
        size: Int = 20
    ): PageResponse<TrendingCoverResponse> {
        // 기간 시작 시점 계산
        val startDate = when (period) {
            TrendingPeriod.DAILY -> LocalDateTime.now().toLocalDate().atStartOfDay()
            TrendingPeriod.WEEKLY -> LocalDateTime.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toLocalDate().atStartOfDay()
            TrendingPeriod.MONTHLY -> LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay()
        }

        // 기간 내 좋아요 수 조회
        val periodLikeCounts = coverLikeRepository.countLikesByPeriod(startDate)
            .associate { array -> (array[0] as Long) to (array[1] as Long) }

        // 모든 커버 가져와서 증가량 계산 (좋아요 없어도 포함)
        val trendingCovers = coverRepository.findAll()
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
                createdAt = cover.createdAt?.format(dateFormatter) ?: ""
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
            val tags = coverTagRepository.findAllByCoverId(cover.id!!)
                .map { it.tag.name }

            CoverListResponse(
                coverId = cover.id!!,
                musicId = cover.musicId,
                userId = cover.userId,
                coverArtist = cover.coverArtist,
                coverTitle = cover.coverTitle,
                coverGenre = cover.coverGenre,
                link = cover.link,
                viewCount = cover.viewCount,
                likeCount = cover.likeCount,
                commentCount = cover.commentCount,
                tags = tags,
                createdAt = cover.createdAt?.format(dateFormatter) ?: ""
            )
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

}