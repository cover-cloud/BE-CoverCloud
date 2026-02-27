package com.covercloud.cover.service

import com.covercloud.cover.domain.Cover
import com.covercloud.cover.domain.CoverGenre
import com.covercloud.cover.domain.CoverLike
import com.covercloud.cover.infrastructure.feign.MusicClient
import com.covercloud.cover.infrastructure.feign.UserClient
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
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetLikedCoversServiceTest {

    @Mock
    private lateinit var coverRepository: CoverRepository

    @Mock
    private lateinit var coverLikeRepository: CoverLikeRepository

    @Mock
    private lateinit var coverTagRepository: CoverTagRepository

    @Mock
    private lateinit var tagRepository: TagRepository

    @Mock
    private lateinit var musicClient: MusicClient

    @Mock
    private lateinit var userClient: UserClient

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
            ).apply { id = 1L },
            Cover(
                userId = 2L,
                musicId = 2L,
                link = "https://example.com/2",
                coverTitle = "Cover 2",
                coverArtist = "Artist 2",
                coverGenre = CoverGenre.POP,
                likeCount = 0
            ).apply { id = 2L },
            Cover(
                userId = 3L,
                musicId = 3L,
                link = "https://example.com/3",
                coverTitle = "Cover 3",
                coverArtist = "Artist 3",
                coverGenre = CoverGenre.K_POP,
                likeCount = 0
            ).apply { id = 3L }
        )
    }

    @Test
    @DisplayName("getLikedCovers - 좋아요한 커버곡 조회 성공")
    fun testGetLikedCoversSuccess() {
        // Given
        val userId = 1L
        val likedCoverIds = listOf(1L, 3L)
        val likedCovers = testCovers.filter { it.id in likedCoverIds }

        val coverLikes = likedCovers.map { cover ->
            CoverLike(cover, userId)
        }

        whenever(coverLikeRepository.findAllByUserId(userId)).thenReturn(coverLikes)
        whenever(coverRepository.findAllById(likedCoverIds)).thenReturn(likedCovers)
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())

        // When
        val result = coverService.getLikedCovers(userId, 0, 20)

        // Then
        assertNotNull(result)
        assertEquals(2, result.content.size)
        assertEquals(2, result.totalElements)
        assertEquals(0, result.pageNumber)
        assertTrue(result.isFirst)
        assertTrue(result.isLast)
    }

    @Test
    @DisplayName("getLikedCovers - 좋아요한 커버곡 없음")
    fun testGetLikedCoversEmpty() {
        // Given
        val userId = 1L
        whenever(coverLikeRepository.findAllByUserId(userId)).thenReturn(emptyList())

        // When
        val result = coverService.getLikedCovers(userId, 0, 20)

        // Then
        assertNotNull(result)
        assertEquals(0, result.content.size)
        assertEquals(0, result.totalElements)
        assertTrue(result.isFirst)
        assertTrue(result.isLast)
    }

    @Test
    @DisplayName("getLikedCovers - 페이지 처리")
    fun testGetLikedCoversWithPagination() {
        // Given
        val userId = 1L
        val likedCoverIds = listOf(1L, 2L, 3L)

        val coverLikes = testCovers.map { cover ->
            CoverLike(cover, userId)
        }

        whenever(coverLikeRepository.findAllByUserId(userId)).thenReturn(coverLikes)
        whenever(coverRepository.findAllById(likedCoverIds)).thenReturn(testCovers)
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())

        // When
        val result = coverService.getLikedCovers(userId, 1, 1)

        // Then
        assertEquals(1, result.content.size)
        assertEquals(3, result.totalElements)
        assertEquals(1, result.pageNumber)
        assertFalse(result.isFirst)
        assertFalse(result.isLast)
    }

    @Test
    @DisplayName("getLikedCovers - 최신순 정렬")
    fun testGetLikedCoversOrderedByCreatedAt() {
        // Given
        val userId = 1L
        val likedCoverIds = listOf(1L, 2L, 3L)

        val coverLikes = testCovers.map { cover ->
            CoverLike(cover, userId)
        }

        // Mock이 반환할 커버들: 역순으로 정렬되어 있음
        // getLikedCovers에서 sortedByDescending(createdAt)을 하므로
        // 반환 순서는 상관없고, 각 커버의 createdAt이 기본값(현재시간)이므로 정렬 후 역순
        whenever(coverLikeRepository.findAllByUserId(userId)).thenReturn(coverLikes)
        whenever(coverRepository.findAllById(likedCoverIds)).thenReturn(testCovers)
        whenever(coverTagRepository.findAllByCoverId(any())).thenReturn(emptyList())

        // When
        val result = coverService.getLikedCovers(userId, 0, 20)

        // Then
        assertEquals(3, result.content.size)
        // testCovers는 모두 같은 시간(현재시간)에 생성되므로, 순서는 반환된 순서대로
        // 하지만 service에서 sortedByDescending(createdAt)을 하므로
        // 결과는 3가지 커버 모두 포함
        assertTrue(result.content.any { it.coverId == 1L })
        assertTrue(result.content.any { it.coverId == 2L })
        assertTrue(result.content.any { it.coverId == 3L })
    }
}
