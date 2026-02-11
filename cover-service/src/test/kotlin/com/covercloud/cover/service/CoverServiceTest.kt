package com.covercloud.cover.service

import com.covercloud.cover.controller.dto.SearchSort
import com.covercloud.cover.domain.Cover
import com.covercloud.cover.domain.CoverGenre
import com.covercloud.cover.domain.TrendingPeriod
import com.covercloud.cover.infrastructure.dto.UserProfileDto
import com.covercloud.cover.infrastructure.feign.MusicClient
import com.covercloud.cover.infrastructure.feign.UserClient
import com.covercloud.cover.repository.CoverRepository
import com.covercloud.cover.repository.CoverLikeRepository
import com.covercloud.cover.repository.CoverTagRepository
import com.covercloud.cover.repository.TagRepository
import com.covercloud.shared.response.ApiResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
    private lateinit var userClient: UserClient

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
                coverGenre = CoverGenre.K_POP,
                likeCount = 0
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
                coverGenre = CoverGenre.POP,
                likeCount = 5
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
                coverGenre = CoverGenre.K_POP,
                likeCount = 10
            ).apply {
                id = 3L
                likeCount = 200
            },

        )
    }

    @Test
    @DisplayName("getTrendingCovers - period=null일 때 전체 기간 조회")
    fun testGetTrendingCoversWithNullPeriod() {
        // Given
        // 전체 조회를 시뮬레이션하기 위해 mock 설정
        whenever(coverRepository.findAll()).thenReturn(testCovers)

        // 좋아요 수 집계나 태그 조회는 빈 리스트로 응답 (로직에 따라 필요시 수정)
        whenever(coverLikeRepository.countLikesByPeriod(any())).thenReturn(emptyList())
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())

        // When
        // period 파라미터를 null로 전달
        val result = coverService.getTrendingCovers(null, 0, 10, null)

        // Then
        assertNotNull(result)
        // setup에서 설정한 testCovers의 개수가 3개이므로 3이 나와야 함
        assertEquals(3, result.content.size)
        // findAll()이 실제로 호출되었는지 검증
        verify(coverRepository).findAll()
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
    @DisplayName("searchCoversByTitle - 부분 검색")
    fun testSearchCoversByTitlePartialMatch() {
        // Given
        val searchTitle = "Cover"
        val filteredCovers = testCovers
        val page = PageImpl(filteredCovers, PageRequest.of(0, 20), 3)

        whenever(coverRepository.findByCoverTitleContainingIgnoreCase(eq(searchTitle), any())).thenReturn(page)
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())

        // When
        val result = coverService.searchCoversByTitle(searchTitle, 0, 20, SearchSort.LATEST)

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

        whenever(coverRepository.findByCoverTitleContainingIgnoreCase(eq(searchTitle), any())).thenReturn(page)

        // When
        val result = coverService.searchCoversByTitle(searchTitle, 0, 20, SearchSort.LATEST)

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
        val result = coverService.searchCoversByTags(searchTag, 0, 20, SearchSort.LATEST)
        println("ajkldf"+ result.content)

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
        val result = coverService.searchCoversByTags(searchTag, 0, 20, SearchSort.LATEST)

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
        val result = coverService.searchCoversByTags(searchTag, 0, 20, SearchSort.LATEST)

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

        whenever(coverRepository.findByCoverTitleContainingIgnoreCase(eq(searchTitle), any())).thenReturn(page)
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())

        // When
        val result = coverService.searchCoversByTitle(searchTitle, 0, 2,SearchSort.LATEST)

        // Then
        assertEquals(2, result.content.size)
        assertEquals(3, result.totalElements)
        assertEquals(2, result.totalPages)
        assertEquals(0, result.pageNumber)
        assertTrue(result.isFirst)
        assertFalse(result.isLast)
    }


    @Test
    @DisplayName("searchCoversByTitle - 인기순 정렬")
    fun testSearchCoversByTitleWithPopularSort() {
        // Given
        val searchTitle = "Cover"
        // 인기순(좋아요 많은 순)으로 정렬된 커버들
        val sortedByPopularity = listOf(
            testCovers[2], // likeCount = 200
            testCovers[0], // likeCount = 100
            testCovers[1]  // likeCount = 50
        )
        val page = PageImpl(sortedByPopularity, PageRequest.of(0, 20), 3)

        whenever(coverRepository.findByCoverTitleContainingIgnoreCase(eq(searchTitle), any())).thenReturn(page)
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())
        mockUserClient()

        // When
        val result = coverService.searchCoversByTitle(searchTitle, 0, 20, SearchSort.POPULAR)
        println("Asdfkjlfd" + result)

        // Then
        assertNotNull(result)
        assertEquals(3, result.content.size)
        // 인기순으로 정렬되어 있는지 확인: 좋아요 개수가 내림차순이어야 함
        assertEquals(200, result.content[0].likeCount)
        assertEquals(100, result.content[1].likeCount)
        assertEquals(50, result.content[2].likeCount)
        verify(coverRepository).findByCoverTitleContainingIgnoreCase(eq(searchTitle), any())
    }

    @Test
    @DisplayName("searchCoversByTitle - 인기순 정렬 페이징")
    fun testSearchCoversByTitleWithPopularSortPaging() {
        // Given
        val searchTitle = "Cover"
        // 첫 페이지: 인기순으로 상위 2개만 반환
        val sortedByPopularity = listOf(
            testCovers[2], // likeCount = 200
            testCovers[0]  // likeCount = 100
        )
        val page = PageImpl(sortedByPopularity, PageRequest.of(0, 2), 3)

        whenever(coverRepository.findByCoverTitleContainingIgnoreCase(eq(searchTitle), any())).thenReturn(page)
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())
        mockUserClient()

        // When
        val result = coverService.searchCoversByTitle(searchTitle, 0, 2, SearchSort.POPULAR)

        // Then
        assertNotNull(result)
        assertEquals(2, result.content.size)
        assertEquals(3, result.totalElements)
        assertEquals(2, result.totalPages)
        assertEquals(0, result.pageNumber)
        assertTrue(result.isFirst)
        assertFalse(result.isLast)
        // 첫 번째 페이지가 인기순으로 정렬되어 있는지 확인
        assertEquals(200, result.content[0].likeCount)
        assertEquals(100, result.content[1].likeCount)
    }

    @Test
    @DisplayName("searchCoversByTitle - 인기순 vs 최신순 비교")
    fun testSearchCoversByTitlePopularVsLatest() {
        // Given
        val searchTitle = "Cover"

        // 인기순 정렬 결과
        val sortedByPopularity = listOf(
            testCovers[2], // likeCount = 200, id = 3
            testCovers[0], // likeCount = 100, id = 1
            testCovers[1]  // likeCount = 50, id = 2
        )

        whenever(coverRepository.findByCoverTitleContainingIgnoreCase(eq(searchTitle), any())).thenReturn(
            PageImpl(sortedByPopularity, PageRequest.of(0, 20), 3)
        )
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())
        mockUserClient()

        // When - 인기순 검색
        val resultPopular = coverService.searchCoversByTitle(searchTitle, 0, 20, SearchSort.POPULAR)

        // Then - 인기순 검색 결과 확인
        assertEquals(3, resultPopular.content.size)
        assertEquals(200, resultPopular.content[0].likeCount)
        assertEquals(100, resultPopular.content[1].likeCount)
        assertEquals(50, resultPopular.content[2].likeCount)
    }

    @Test
    @DisplayName("deleteCover - 좋아요가 있는 커버 삭제 성공")
    fun testDeleteCoverWithLikes() {
        // Given
        val coverId = 1L
        val cover = testCovers[0]

        whenever(coverRepository.findById(coverId)).thenReturn(Optional.of(cover))

        // When
        coverService.deleteCover(coverId)

        // Then
        verify(coverLikeRepository).deleteAllByCoverId(coverId)
        verify(coverTagRepository).deleteAllByCoverId(cover.id!!)
        verify(coverRepository).delete(cover)
    }

    @Test
    @DisplayName("deleteCover - 좋아요 없는 커버 삭제 성공")
    fun testDeleteCoverWithoutLikes() {
        // Given
        val coverId = 2L
        val cover = Cover(
            userId = 2L,
            musicId = 2L,
            link = "https://example.com/2",
            coverTitle = "Cover 2",
            coverArtist = "Artist 2",
            coverGenre = CoverGenre.POP
        ).apply {
            id = coverId
            likeCount = 0
        }

        whenever(coverRepository.findById(coverId)).thenReturn(Optional.of(cover))

        // When
        coverService.deleteCover(coverId)

        // Then
        verify(coverLikeRepository).deleteAllByCoverId(coverId)
        verify(coverTagRepository).deleteAllByCoverId(cover.id!!)
        verify(coverRepository).delete(cover)
    }


    // ============ getCovers 테스트 ============

    @Test
    @DisplayName("getCovers - 전체 커버 조회 (기간 없음, 장르 없음)")
    fun testGetCoversAll() {
        // Given
        val page = 0
        val size = 20
        val pageable = PageRequest.of(page, size)

        whenever(coverRepository.findCovers(
            startDate = anyOrNull(), // null이 들어올 수 있다면 anyOrNull 사용
            genres = anyOrNull(),
            pageable = eq(pageable)
        )).thenReturn(PageImpl(testCovers, pageable, testCovers.size.toLong()))

        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())
        whenever(coverLikeRepository.existsByCoverIdAndUserId(any(), any())).thenReturn(false)
        mockUserClient()

        // When
        val result = coverService.getCovers(period = null, page = page, size = size, genres = null, userId = null)

        // Then
        assertEquals(3, result.content.size)
        assertEquals(0, result.pageNumber)
        assertEquals(20, result.pageSize)
        assertEquals(3, result.totalElements)
        assertTrue(result.isFirst)
        assertTrue(result.isLast)
    }

    @Test
    @DisplayName("getCovers - 일일(DAILY) 트렌딩 커버 조회")
    fun testGetCoversDailyTrending() {
        // Given
        val page = 0
        val size = 20
        val pageable = PageRequest.of(page, size)

        // DAILY period일 때 계산되는 startDate와 동일하게 설정
        val todayStartOfDay = java.time.LocalDateTime.now().toLocalDate().atStartOfDay()

        whenever(coverRepository.findCovers(
            startDate = eq(todayStartOfDay),
            genres = anyOrNull(),
            pageable = eq(pageable)
        )).thenReturn(PageImpl(testCovers, pageable, testCovers.size.toLong()))

        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())
        whenever(coverLikeRepository.existsByCoverIdAndUserId(any(), any())).thenReturn(false)
        mockUserClient()

        // When
        val result = coverService.getCovers(
            period = TrendingPeriod.DAILY,
            page = page,
            size = size,
            genres = null,
            userId = null
        )

        // Then
        assertEquals(3, result.content.size)
    }

    @Test
    @DisplayName("getCovers - 특정 장르(K-POP) 필터링")
    fun testGetCoversWithGenreFilter() {
        // Given
        val page = 0
        val size = 20
        val pageable = PageRequest.of(page, size)
        val kpopCovers = testCovers.filter { it.coverGenre == CoverGenre.K_POP }

        whenever(coverRepository.findCovers(
            startDate = anyOrNull(),
            genres = anyOrNull(),
            pageable = eq(pageable)
        )).thenReturn(PageImpl(kpopCovers, pageable, kpopCovers.size.toLong()))

        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())
        whenever(coverLikeRepository.existsByCoverIdAndUserId(any(), any())).thenReturn(false)
        mockUserClient()

        // When
        val result = coverService.getCovers(
            period = null,
            page = page,
            size = size,
            genres = listOf("k-pop"),
            userId = null
        )

        // Then
        assertEquals(2, result.content.size)
    }

    @Test
    @DisplayName("getCovers - 여러 장르 필터링 (K-POP, POP)")
    fun testGetCoversWithMultipleGenres() {
        // Given
        val page = 0
        val size = 20
        val pageable = PageRequest.of(page, size)

        whenever(coverRepository.findCovers(
            startDate = anyOrNull(),
            genres = anyOrNull(),
            pageable = eq(pageable)
        )).thenReturn(PageImpl(testCovers, pageable, testCovers.size.toLong()))

        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())
        whenever(coverLikeRepository.existsByCoverIdAndUserId(any(), any())).thenReturn(false)
        mockUserClient()

        // When
        val result = coverService.getCovers(
            period = null,
            page = page,
            size = size,
            genres = listOf("k-pop", "pop"),
            userId = null
        )

        // Then
        assertEquals(3, result.content.size)
    }

    @Test
    @DisplayName("getCovers - 페이지 처리 (2페이지, 크기 1)")
    fun testGetCoversWithPagination() {
        // Given
        val page = 1
        val size = 1
        val pageable = PageRequest.of(page, size)
        val secondCover = listOf(testCovers[1])

        whenever(coverRepository.findCovers(
            startDate = anyOrNull(),
            genres = anyOrNull(),
            pageable = eq(pageable)
        )).thenReturn(PageImpl(secondCover, pageable, testCovers.size.toLong()))

        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())
        whenever(coverLikeRepository.existsByCoverIdAndUserId(any(), any())).thenReturn(false)
        mockUserClient()

        // When
        val result = coverService.getCovers(
            period = null,
            page = page,
            size = size,
            genres = null,
            userId = null
        )

        // Then
        assertEquals(1, result.content.size)
        assertEquals(1, result.pageNumber)
        assertEquals(1, result.pageSize)
        assertEquals(3, result.totalElements)
        assertFalse(result.isFirst)
        assertFalse(result.isLast)
    }

    @Test
    @DisplayName("getCovers - 사용자별 좋아요 상태 확인")
    fun testGetCoversWithUserLikeStatus() {
        // Given
        val userId = 1L
        val page = 0
        val size = 20
        val pageable = PageRequest.of(page, size)

        whenever(coverRepository.findCovers(
            startDate = anyOrNull(),
            genres = anyOrNull(),
            pageable = eq(pageable)
        )).thenReturn(PageImpl(testCovers, pageable, testCovers.size.toLong()))

        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())
        whenever(coverLikeRepository.existsByCoverIdAndUserId(eq(1L), eq(userId))).thenReturn(true)
        whenever(coverLikeRepository.existsByCoverIdAndUserId(eq(2L), eq(userId))).thenReturn(false)
        whenever(coverLikeRepository.existsByCoverIdAndUserId(eq(3L), eq(userId))).thenReturn(true)
        mockUserClient()

        // When
        val result = coverService.getCovers(
            period = null,
            page = page,
            size = size,
            genres = null,
            userId = userId
        )

        // Then
        assertEquals(3, result.content.size)
        assertTrue(result.content[0].isLiked)
        assertFalse(result.content[1].isLiked)
        assertTrue(result.content[2].isLiked)
    }

    @Test
    @DisplayName("getCovers - 삭제된 사용자의 익명 처리")
    fun testGetCoversWithDeletedUserAnonymous() {
        // Given
        val page = 0
        val size = 20
        val pageable = PageRequest.of(page, size)

        whenever(coverRepository.findCovers(
            startDate = anyOrNull(),
            genres = anyOrNull(),
            pageable = eq(pageable)
        )).thenReturn(PageImpl(testCovers, pageable, testCovers.size.toLong()))

        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())
        whenever(coverLikeRepository.existsByCoverIdAndUserId(any(), any())).thenReturn(false)

        // 첫 번째 사용자는 삭제됨
        mockUserClientWithDeletedUser(userId = 1L, isDeleted = true)
        // 나머지는 정상
        mockUserClientWithDeletedUser(userId = 2L, isDeleted = false)
        mockUserClientWithDeletedUser(userId = 3L, isDeleted = false)

        // When
        val result = coverService.getCovers(
            period = null,
            page = page,
            size = size,
            genres = null,
            userId = null
        )

        // Then
        assertEquals("익명 사용자", result.content[0].nickname)
        assertTrue(result.content[0].isAuthorDeleted)
    }

    @Test
    @DisplayName("getCovers - 주간(WEEKLY) 트렌딩 조회")
    fun testGetCoversWeeklyTrending() {
        // Given
        val page = 0
        val size = 20
        val pageable = PageRequest.of(page, size)

        // WEEKLY period일 때 계산되는 startDate와 동일하게 설정
        val weekStartOfDay = java.time.LocalDateTime.now()
            .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            .toLocalDate().atStartOfDay()

        whenever(coverRepository.findCovers(
            startDate = eq(weekStartOfDay),
            genres = anyOrNull(),
            pageable = eq(pageable)
        )).thenReturn(PageImpl(testCovers, pageable, testCovers.size.toLong()))

        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())
        whenever(coverLikeRepository.existsByCoverIdAndUserId(any(), any())).thenReturn(false)
        mockUserClient()

        // When
        val result = coverService.getCovers(
            period = TrendingPeriod.WEEKLY,
            page = page,
            size = size,
            genres = null,
            userId = null
        )

        // Then
        assertEquals(3, result.content.size)
    }

    @Test
    @DisplayName("getCovers - 월간(MONTHLY) 트렌딩 조회")
    fun testGetCoversMonthlyTrending() {
        // Given
        val page = 0
        val size = 20
        val pageable = PageRequest.of(page, size)

        // MONTHLY period일 때 계산되는 startDate와 동일하게 설정
        val monthStartOfDay = java.time.LocalDateTime.now()
            .withDayOfMonth(1)
            .toLocalDate().atStartOfDay()

        whenever(coverRepository.findCovers(
            startDate = eq(monthStartOfDay),
            genres = anyOrNull(),
            pageable = eq(pageable)
        )).thenReturn(PageImpl(testCovers, pageable, testCovers.size.toLong()))

        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())
        whenever(coverLikeRepository.existsByCoverIdAndUserId(any(), any())).thenReturn(false)
        mockUserClient()

        // When
        val result = coverService.getCovers(
            period = TrendingPeriod.MONTHLY,
            page = page,
            size = size,
            genres = null,
            userId = null
        )

        // Then
        assertEquals(3, result.content.size)
    }

    @Test
    @DisplayName("getCovers - 빈 결과 반환")
    fun testGetCoversEmpty() {
        // Given
        val page = 0
        val size = 20
        val pageable = PageRequest.of(page, size)

        whenever(coverRepository.findCovers(
            startDate = anyOrNull(),
            genres = anyOrNull(),
            pageable = eq(pageable)
        )).thenReturn(PageImpl(emptyList(), pageable, 0L))

        // When
        val result = coverService.getCovers(
            period = null,
            page = page,
            size = size,
            genres = null,
            userId = null
        )

        // Then
        assertEquals(0, result.content.size)
        assertEquals(0, result.totalElements)
        assertTrue(result.isFirst)
        assertTrue(result.isLast)
    }

    // ============ 헬퍼 메서드 ============

    private fun mockUserClient() {
        val userProfiles = mapOf(
            1L to UserProfileDto(userId = 1L, nickname = "User1", profileImageUrl = "https://example.com/1.jpg", isDeleted = false),
            2L to UserProfileDto(userId = 2L, nickname = "User2", profileImageUrl = "https://example.com/2.jpg", isDeleted = false),
            3L to UserProfileDto(userId = 3L, nickname = "User3", profileImageUrl = "https://example.com/3.jpg", isDeleted = false)
        )

        userProfiles.forEach { (userId, profile) ->
            whenever(userClient.getUserProfile(userId)).thenReturn(
                ApiResponse(success = true, data = profile)
            )
        }
    }

    private fun mockUserClientWithDeletedUser(userId: Long, isDeleted: Boolean) {
        val profile = if (isDeleted) {
            UserProfileDto(userId = userId, nickname = "삭제된 사용자", profileImageUrl = null, isDeleted = true)
        } else {
            UserProfileDto(userId = userId, nickname = "User$userId", profileImageUrl = "https://example.com/$userId.jpg", isDeleted = false)
        }

        whenever(userClient.getUserProfile(userId)).thenReturn(
            ApiResponse(success = true, data = profile)
        )
    }

}
