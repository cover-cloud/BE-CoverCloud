package com.covercloud.cover.service

import com.covercloud.cover.domain.Cover
import com.covercloud.cover.domain.CoverGenre
import com.covercloud.cover.repository.CoverRepository
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
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

@ExtendWith(MockitoExtension::class)
class CoverServiceCommentTest {

    @Mock
    private lateinit var coverRepository: CoverRepository

    @Mock
    private lateinit var tagRepository: com.covercloud.cover.repository.TagRepository

    @Mock
    private lateinit var coverTagRepository: com.covercloud.cover.repository.CoverTagRepository

    @Mock
    private lateinit var musicClient: com.covercloud.cover.infrastructure.feign.MusicClient

    @Mock
    private lateinit var userClient: com.covercloud.cover.infrastructure.feign.UserClient

    @Mock
    private lateinit var coverLikeRepository: com.covercloud.cover.repository.CoverLikeRepository

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
                likeCount = 10
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
                likeCount = 20
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
                likeCount = 30
            }
        )
    }

    @Test
    @DisplayName("getCoversByUserComments - 사용자가 댓글을 단 커버들 조회 성공")
    fun testGetCoversByUserCommentsSuccess() {
        // Given
        val userId = 1L
        val page = 0
        val size = 20
        val pagedCovers = PageImpl(listOf(testCovers[0], testCovers[1]), PageRequest.of(page, size), 2)

        whenever(coverRepository.findCoversByUserComments(eq(userId), any<Pageable>())).thenReturn(pagedCovers)
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())
        whenever(userClient.getUserProfile(eq(testCovers[0].userId))).thenReturn(
            com.covercloud.shared.response.ApiResponse(
                success = true,
                data = com.covercloud.cover.infrastructure.dto.UserProfileDto(
                    userId = 1L,
                    nickname = "Test User",
                    profileImageUrl = "https://example.com/profile.jpg",
                    isDeleted = false
                )
            )
        )
        whenever(userClient.getUserProfile(eq(testCovers[1].userId))).thenReturn(
            com.covercloud.shared.response.ApiResponse(
                success = true,
                data = com.covercloud.cover.infrastructure.dto.UserProfileDto(
                    userId = 2L,
                    nickname = "Test User 2",
                    profileImageUrl = "https://example.com/profile2.jpg",
                    isDeleted = false
                )
            )
        )

        // When
        val result = coverService.getCoversByUserComments(userId, page, size)

        // Then
        assertNotNull(result)
        assertEquals(2, result.content.size)
        assertEquals(2, result.totalElements)
        assertTrue(result.isFirst)
        assertTrue(result.isLast)
    }

    @Test
    @DisplayName("getCoversByUserComments - 페이징 처리 확인")
    fun testGetCoversByUserCommentsWithPagination() {
        // Given
        val userId = 1L
        val page = 0
        val size = 2
        val pagedCovers = PageImpl(
            listOf(testCovers[0], testCovers[1]),
            PageRequest.of(page, size),
            3
        )

        whenever(coverRepository.findCoversByUserComments(eq(userId), any<Pageable>())).thenReturn(pagedCovers)
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())
        whenever(userClient.getUserProfile(any())).thenReturn(
            com.covercloud.shared.response.ApiResponse(
                success = true,
                data = com.covercloud.cover.infrastructure.dto.UserProfileDto(
                    userId = 1L,
                    nickname = "Test User",
                    profileImageUrl = null,
                    isDeleted = false
                )
            )
        )

        // When
        val result = coverService.getCoversByUserComments(userId, page, size)

        // Then
        assertEquals(2, result.content.size)
        assertEquals(3, result.totalElements)
        assertEquals(2, result.totalPages)
        assertTrue(result.isFirst)
        assertFalse(result.isLast)
    }

    @Test
    @DisplayName("getCoversByUserComments - 정렬 확인 (DESC)")
    fun testGetCoversByUserCommentsOrderByDesc() {
        // Given
        val userId = 1L
        val sortDirection = "DESC"
        val pagedCovers = PageImpl(
            listOf(testCovers[2], testCovers[1], testCovers[0]),
            PageRequest.of(0, 20),
            3
        )

        whenever(coverRepository.findCoversByUserComments(eq(userId), any<Pageable>())).thenReturn(pagedCovers)
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())
        whenever(userClient.getUserProfile(any())).thenReturn(
            com.covercloud.shared.response.ApiResponse(
                success = true,
                data = com.covercloud.cover.infrastructure.dto.UserProfileDto(
                    userId = 1L,
                    nickname = "Test User",
                    profileImageUrl = null,
                    isDeleted = false
                )
            )
        )

        // When
        val result = coverService.getCoversByUserComments(userId, 0, 20, "createdAt", sortDirection)

        // Then
        assertEquals(3, result.content.size)
        // 최신 순서 확인
        assertEquals(testCovers[2].id, result.content[0].coverId)
        assertEquals(testCovers[1].id, result.content[1].coverId)
        assertEquals(testCovers[0].id, result.content[2].coverId)
    }

    @Test
    @DisplayName("getCoversByUserComments - 댓글이 없는 사용자")
    fun testGetCoversByUserCommentsEmpty() {
        // Given
        val userId = 999L
        val emptyPage = PageImpl<Cover>(emptyList(), PageRequest.of(0, 20), 0)

        whenever(coverRepository.findCoversByUserComments(eq(userId), any<Pageable>())).thenReturn(emptyPage)

        // When
        val result = coverService.getCoversByUserComments(userId, 0, 20)

        // Then
        assertNotNull(result)
        assertEquals(0, result.content.size)
        assertEquals(0, result.totalElements)
        assertTrue(result.isFirst)
        assertTrue(result.isLast)
    }

    @Test
    @DisplayName("getCoversByUserComments - 삭제된 사용자의 커버는 익명으로 표시")
    fun testGetCoversByUserCommentsWithDeletedAuthor() {
        // Given
        val userId = 1L
        val pagedCovers = PageImpl(listOf(testCovers[0]), PageRequest.of(0, 20), 1)

        whenever(coverRepository.findCoversByUserComments(eq(userId), any<Pageable>())).thenReturn(pagedCovers)
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())
        // 커버 작성자가 삭제된 계정
        whenever(userClient.getUserProfile(eq(testCovers[0].userId))).thenReturn(
            com.covercloud.shared.response.ApiResponse(
                success = true,
                data = com.covercloud.cover.infrastructure.dto.UserProfileDto(
                    userId = testCovers[0].userId,
                    nickname = "Deleted User",
                    profileImageUrl = "https://example.com/profile.jpg",
                    isDeleted = true
                )
            )
        )

        // When
        val result = coverService.getCoversByUserComments(userId, 0, 20)

        // Then
        assertEquals(1, result.content.size)
        assertEquals("익명 사용자", result.content[0].nickname)
        assertNull(result.content[0].profileImage)
        assertTrue(result.content[0].isAuthorDeleted)
    }

    @Test
    @DisplayName("getCoversByUserComments - 정렬 ASC 확인")
    fun testGetCoversByUserCommentsOrderByAsc() {
        // Given
        val userId = 1L
        val sortDirection = "ASC"
        val pagedCovers = PageImpl(
            listOf(testCovers[0], testCovers[1], testCovers[2]),
            PageRequest.of(0, 20),
            3
        )

        whenever(coverRepository.findCoversByUserComments(eq(userId), any<Pageable>())).thenReturn(pagedCovers)
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())
        whenever(userClient.getUserProfile(any())).thenReturn(
            com.covercloud.shared.response.ApiResponse(
                success = true,
                data = com.covercloud.cover.infrastructure.dto.UserProfileDto(
                    userId = 1L,
                    nickname = "Test User",
                    profileImageUrl = null,
                    isDeleted = false
                )
            )
        )

        // When
        val result = coverService.getCoversByUserComments(userId, 0, 20, "createdAt", sortDirection)

        // Then
        assertEquals(3, result.content.size)
        // 오래된 순서 확인
        assertEquals(testCovers[0].id, result.content[0].coverId)
        assertEquals(testCovers[1].id, result.content[1].coverId)
        assertEquals(testCovers[2].id, result.content[2].coverId)
    }

    @Test
    @DisplayName("getCoversByUserComments - 여러 페이지 조회")
    fun testGetCoversByUserCommentsMultiplePages() {
        // Given
        val userId = 1L
        val size = 1
        val page2Covers = PageImpl(listOf(testCovers[1]), PageRequest.of(1, size), 3)

        whenever(coverRepository.findCoversByUserComments(eq(userId), any<Pageable>())).thenReturn(page2Covers)
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())
        whenever(userClient.getUserProfile(any())).thenReturn(
            com.covercloud.shared.response.ApiResponse(
                success = true,
                data = com.covercloud.cover.infrastructure.dto.UserProfileDto(
                    userId = 1L,
                    nickname = "Test User",
                    profileImageUrl = null,
                    isDeleted = false
                )
            )
        )

        // When
        val result = coverService.getCoversByUserComments(userId, 1, size)

        // Then
        assertEquals(1, result.content.size)
        assertEquals(3, result.totalElements)
        assertEquals(3, result.totalPages)
        assertFalse(result.isFirst)
        assertFalse(result.isLast)
    }
}
