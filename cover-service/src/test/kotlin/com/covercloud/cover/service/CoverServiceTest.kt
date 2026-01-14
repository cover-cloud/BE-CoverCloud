package com.covercloud.cover.service

import com.covercloud.cover.domain.Cover
import com.covercloud.cover.domain.CoverGenre
import com.covercloud.cover.domain.TrendingPeriod
import com.covercloud.cover.infrastructure.feign.MusicClient
import com.covercloud.cover.repository.CoverRepository
import com.covercloud.cover.repository.CoverLikeRepository
import com.covercloud.cover.repository.CoverTagRepository
import com.covercloud.cover.repository.TagRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class CoverServiceTest {

    @Mock
    private lateinit var coverRepository: CoverRepository

    @Mock
    private lateinit var tagRepository: TagRepository

    @Mock
    private lateinit var coverTagRepository: CoverTagRepository

    @Mock
    private lateinit var musicClient: MusicClient

    @Mock
    private lateinit var coverLikeRepository: CoverLikeRepository

    @InjectMocks
    private lateinit var coverService: CoverService

    private lateinit var testCovers: List<Cover>

    @BeforeEach
    fun setup() {
        testCovers = listOf(
            Cover(
                userId = 1L,
                musicId = 1L,
                link = "https://example.com/1",
                coverTitle = "Cover 1",
                coverArtist = "Artist 1",
                coverGenre = CoverGenre.K_POP
            ).apply { 
                id = 1L
                likeCount = 100
            },
            Cover(
                userId = 2L,
                musicId = 2L,
                link = "https://example.com/2",
                coverTitle = "Cover 2",
                coverArtist = "Artist 2",
                coverGenre = CoverGenre.POP
            ).apply { 
                id = 2L
                likeCount = 50
            },
            Cover(
                userId = 3L,
                musicId = 3L,
                link = "https://example.com/3",
                coverTitle = "Cover 3",
                coverArtist = "Artist 3",
                coverGenre = CoverGenre.K_POP
            ).apply { 
                id = 3L
                likeCount = 200
            }
        )
    }

    @Test
    @DisplayName("getTrendingCovers - period=null일 때 전체 기간 조회")
    fun testGetTrendingCoversWithNullPeriod() {
        // Given
        whenever(coverRepository.findAll()).thenReturn(testCovers)
        whenever(coverLikeRepository.countLikesByPeriod(any())).thenReturn(emptyList())
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())

        // When
        val result = coverService.getTrendingCovers(null, 0, 10, null)

        // Then
        assertNotNull(result)
        assertEquals(3, result.content.size)
    }

    @Test
    @DisplayName("getTrendingCovers - period=DAILY일 때 일간 조회")
    fun testGetTrendingCoversWithDailyPeriod() {
        // Given
        whenever(coverRepository.findAll()).thenReturn(testCovers)
        whenever(coverLikeRepository.countLikesByPeriod(any())).thenReturn(emptyList())
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())

        // When
        val result = coverService.getTrendingCovers(TrendingPeriod.DAILY, 0, 10, null)

        // Then
        assertNotNull(result)
        assertEquals(3, result.content.size)
    }

    @Test
    @DisplayName("getTrendingCovers - genre 필터링 없이 모든 장르 조회")
    fun testGetTrendingCoversWithoutGenreFilter() {
        // Given
        whenever(coverRepository.findAll()).thenReturn(testCovers)
        whenever(coverLikeRepository.countLikesByPeriod(any())).thenReturn(emptyList())
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())

        // When
        val result = coverService.getTrendingCovers(TrendingPeriod.WEEKLY, 0, 10, null)

        // Then
        assertEquals(3, result.content.size)  // ROCK 2개 + HIPHOP 1개
    }

    @Test
    @DisplayName("getTrendingCovers - genre=K_POP으로 필터링")
    fun testGetTrendingCoversWithRockGenreFilter() {
        // Given
        whenever(coverRepository.findAll()).thenReturn(testCovers)
        whenever(coverLikeRepository.countLikesByPeriod(any())).thenReturn(emptyList())
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())

        // When
    val result = coverService.getTrendingCovers(TrendingPeriod.WEEKLY, 0, 10, listOf("K_POP"))

        // Then
        assertEquals(2, result.content.size)
        assertTrue(result.content.all { it.coverGenre == CoverGenre.K_POP })
    }

    @Test
    @DisplayName("getTrendingCovers - 페이징 처리 확인")
    fun testGetTrendingCoversWithPagination() {
        // Given
        whenever(coverRepository.findAll()).thenReturn(testCovers)
        whenever(coverLikeRepository.countLikesByPeriod(any())).thenReturn(emptyList())
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())

        // When
        val result = coverService.getTrendingCovers(null, 0, 2, null)

        // Then
        assertEquals(2, result.content.size)
        assertEquals(3, result.totalElements)
        assertEquals(2, result.totalPages)
    }

    @Test
    @DisplayName("getTrendingCovers - 좋아요 수 내림차순 정렬 확인")
    fun testGetTrendingCoversOrderByLikes() {
        // Given
        val likeCounts = listOf(
            arrayOf<Any>(1L, 10L),  // cover 1: 기간 내 10개
            arrayOf<Any>(2L, 5L),   // cover 2: 기간 내 5개
            arrayOf<Any>(3L, 20L)   // cover 3: 기간 내 20개
        )
        whenever(coverRepository.findAll()).thenReturn(testCovers)
        whenever(coverLikeRepository.countLikesByPeriod(any())).thenReturn(likeCounts)
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())

        // When
        val result = coverService.getTrendingCovers(TrendingPeriod.WEEKLY, 0, 10, null)

        // Then
        assertEquals(3, result.content.size)
        // 기간 내 좋아요 수 기준 정렬: 20 > 10 > 5
        assertEquals(3L, result.content[0].coverId)  // Cover 3
        assertEquals(1L, result.content[1].coverId)  // Cover 1
        assertEquals(2L, result.content[2].coverId)  // Cover 2
    }
}
