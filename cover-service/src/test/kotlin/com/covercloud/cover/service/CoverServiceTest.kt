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
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
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

    @Test
    @DisplayName("searchCoversByTitle - Repository 호출 확인")
    fun testSearchCoversByTitle() {
        // Given
        val searchTitle = "Cover 1"
        val filteredCovers = listOf(testCovers[0])
        val page = PageImpl(filteredCovers, PageRequest.of(0, 20), 1)

        whenever(coverRepository.searchByTitle(eq(searchTitle), any())).thenReturn(page)
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())

        // When
        val result = coverService.searchCoversByTitle(searchTitle, 0, 20, "createdAt", "DESC")

        // Then
        assertNotNull(result)
        assertEquals(1, result.content.size)
        verify(coverRepository).searchByTitle(eq(searchTitle), any())
    }

    @Test
    @DisplayName("searchCoversByTitle - 부분 검색")
    fun testSearchCoversByTitlePartialMatch() {
        // Given
        val searchTitle = "Cover"
        val filteredCovers = testCovers
        val page = PageImpl(filteredCovers, PageRequest.of(0, 20), 3)

        whenever(coverRepository.searchByTitle(eq(searchTitle), any())).thenReturn(page)
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())

        // When
        val result = coverService.searchCoversByTitle(searchTitle, 0, 20, "createdAt", "DESC")

        // Then
        assertNotNull(result)
        assertEquals(3, result.content.size)
        assertTrue(result.content.all { it.coverTitle?.contains("Cover") == true })
    }

    @Test
    @DisplayName("searchCoversByTitle - 검색 결과 없음")
    fun testSearchCoversByTitleNoResult() {
        // Given
        val searchTitle = "NonExistent"
        val page = PageImpl<Cover>(emptyList(), PageRequest.of(0, 20), 0)

        whenever(coverRepository.searchByTitle(eq(searchTitle), any())).thenReturn(page)

        // When
        val result = coverService.searchCoversByTitle(searchTitle, 0, 20, "createdAt", "DESC")

        // Then
        assertNotNull(result)
        assertEquals(0, result.content.size)
        assertEquals(0, result.totalElements)
    }

    @Test
    @DisplayName("searchCoversByTags - 태그로 검색 성공")
    fun testSearchCoversByTags() {
        // Given
        val searchTag = "jazz"
        val filteredCovers = listOf(testCovers[0])
        val page = PageImpl(filteredCovers, PageRequest.of(0, 20), 1)

        whenever(coverRepository.searchByTags(eq(searchTag), any())).thenReturn(page)
        whenever(coverTagRepository.findAllByCoverId(eq(1L))).thenReturn(emptyList())

        // When
        val result = coverService.searchCoversByTags(searchTag, 0, 20, "createdAt", "DESC")

        // Then
        assertNotNull(result)
        assertEquals(1, result.content.size)
        verify(coverRepository).searchByTags(eq(searchTag), any())
    }

    @Test
    @DisplayName("searchCoversByTags - 태그 부분 검색")
    fun testSearchCoversByTagsPartialMatch() {
        // Given
        val searchTag = "rock"
        val filteredCovers = testCovers.subList(0, 2)
        val page = PageImpl(filteredCovers, PageRequest.of(0, 20), 2)

        whenever(coverRepository.searchByTags(eq(searchTag), any())).thenReturn(page)
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())

        // When
        val result = coverService.searchCoversByTags(searchTag, 0, 20, "createdAt", "DESC")

        // Then
        assertNotNull(result)
        assertEquals(2, result.content.size)
    }

    @Test
    @DisplayName("searchCoversByTags - 검색 결과 없음")
    fun testSearchCoversByTagsNoResult() {
        // Given
        val searchTag = "nonexistent-tag"
        val page = PageImpl<Cover>(emptyList(), PageRequest.of(0, 20), 0)

        whenever(coverRepository.searchByTags(eq(searchTag), any())).thenReturn(page)

        // When
        val result = coverService.searchCoversByTags(searchTag, 0, 20, "createdAt", "DESC")

        // Then
        assertNotNull(result)
        assertEquals(0, result.content.size)
    }

    @Test
    @DisplayName("searchCoversByTitle - 페이징 처리")
    fun testSearchCoversByTitleWithPagination() {
        // Given
        val searchTitle = "Cover"
        val pagedCovers = listOf(testCovers[0], testCovers[1])
        val page = PageImpl(pagedCovers, PageRequest.of(0, 2), 3)

        whenever(coverRepository.searchByTitle(eq(searchTitle), any())).thenReturn(page)
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())

        // When
        val result = coverService.searchCoversByTitle(searchTitle, 0, 2, "createdAt", "DESC")

        // Then
        assertEquals(2, result.content.size)
        assertEquals(3, result.totalElements)
        assertEquals(2, result.totalPages)
        assertEquals(0, result.pageNumber)
        assertTrue(result.isFirst)
        assertFalse(result.isLast)
    }

    @Test
    @DisplayName("searchCoversByTitle - 정렬 오름차순")
    fun testSearchCoversByTitleWithAscSort() {
        // Given
        val searchTitle = "Cover"
        val filteredCovers = testCovers
        val page = PageImpl(filteredCovers, PageRequest.of(0, 20), 3)

        whenever(coverRepository.searchByTitle(eq(searchTitle), any())).thenReturn(page)
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())

        // When
        val result = coverService.searchCoversByTitle(searchTitle, 0, 20, "createdAt", "ASC")

        // Then
        assertNotNull(result)
        assertEquals(3, result.content.size)
        verify(coverRepository).searchByTitle(eq(searchTitle), any())
    }
}
