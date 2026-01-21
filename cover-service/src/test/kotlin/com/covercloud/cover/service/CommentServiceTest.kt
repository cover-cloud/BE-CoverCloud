package com.covercloud.cover.service

import com.covercloud.cover.domain.Comment
import com.covercloud.cover.domain.CommentLike
import com.covercloud.cover.domain.Cover
import com.covercloud.cover.domain.CoverGenre
import com.covercloud.cover.repository.CommentRepository
import com.covercloud.cover.repository.CommentLikeRepository
import com.covercloud.cover.repository.CoverRepository
import com.covercloud.cover.service.dto.CommentResponse
import com.covercloud.cover.service.dto.CreateServiceCommentRequest
import com.covercloud.cover.service.dto.UpdateServiceCommentRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.repository.findByIdOrNull
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class CommentServiceTest {

    @Mock
    private lateinit var commentRepository: CommentRepository

    @Mock
    private lateinit var commentLikeRepository: CommentLikeRepository

    @Mock
    private lateinit var coverRepository: CoverRepository

    @Mock
    private lateinit var commentResponseBuilder: CommentResponseBuilder

    @InjectMocks
    private lateinit var commentService: CommentService

    private lateinit var testCover: Cover
    private lateinit var testComment: Comment

    @BeforeEach
    fun setup() {
        testCover = Cover(
            userId = 1L,
            musicId = 1L,
            link = "https://example.com/video1",
            coverArtist = "Artist 1",
            coverTitle = "Cover 1",
            coverGenre = CoverGenre.K_POP
        ).apply { id = 1L }

        testComment = Comment(
            content = "좋은 커버곡입니다!",
            cover = testCover,
            userId = 2L,
            parentCommentId = null
        ).apply { id = 1L }
    }

    @Test
    @DisplayName("toggleLike - 좋아요 추가")
    fun testToggleLikeAdd() {
        // Given
        val commentId = 1L
        val userId = 3L
        val commentResponse = CommentResponse(
            commentId = commentId,
            content = "좋은 커버곡입니다!",
            coverId = 1L,
            userId = 2L,
            likeCount = 1,
            isLiked = true
        )

        whenever(commentRepository.findByIdOrNull(eq(commentId))).thenReturn(testComment)
        whenever(commentLikeRepository.findByCommentIdAndUserId(eq(commentId), eq(userId))).thenReturn(null)
        whenever(commentLikeRepository.save(any<CommentLike>())).thenReturn(CommentLike(comment = testComment, userId = userId))
        whenever(commentResponseBuilder.buildCommentResponse(eq(testComment), eq(userId))).thenReturn(commentResponse)

        // When
        val result = commentService.toggleLike(commentId, userId)

        // Then
        assertNotNull(result)
        assertEquals(1, result.likeCount)
        assertTrue(result.isLiked)
        verify(commentLikeRepository).save(any<CommentLike>())
    }

    @Test
    @DisplayName("toggleLike - 좋아요 취소")
    fun testToggleLikeRemove() {
        // Given
        val commentId = 1L
        val userId = 3L
        val existingLike = CommentLike(testComment, userId).apply { id = 1L }
        val commentResponse = CommentResponse(
            commentId = commentId,
            content = "좋은 커버곡입니다!",
            coverId = 1L,
            userId = 2L,
            likeCount = 0,
            isLiked = false
        )

        whenever(commentRepository.findByIdOrNull(eq(commentId))).thenReturn(testComment)
        whenever(commentLikeRepository.findByCommentIdAndUserId(eq(commentId), eq(userId))).thenReturn(existingLike)
        whenever(commentResponseBuilder.buildCommentResponse(eq(testComment), eq(userId))).thenReturn(commentResponse)

        // When
        val result = commentService.toggleLike(commentId, userId)

        // Then
        assertNotNull(result)
        assertEquals(0, result.likeCount)
        assertTrue(!result.isLiked)
        verify(commentLikeRepository).delete(eq(existingLike))
    }

    @Test
    @DisplayName("addComment - 댓글 추가 성공")
    fun testAddComment() {
        // Given
        val request = CreateServiceCommentRequest(
            content = "새로운 댓글",
            coverId = 1L,
            parentCommentId = null,
            userId = 2L

        )
        val userId = 2L
        val savedComment = Comment(
            content = "새로운 댓글",
            cover = testCover,
            userId = userId,
            parentCommentId = null
        ).apply { id = 2L }
        val commentResponse = CommentResponse(
            commentId = 2L,
            content = "새로운 댓글",
            coverId = 1L,
            userId = userId,
            likeCount = 0,
            isLiked = false
        )

        whenever(coverRepository.findByIdOrNull(eq(1L))).thenReturn(testCover)
        whenever(commentRepository.save(any<Comment>())).thenReturn(savedComment)
        whenever(commentResponseBuilder.buildCommentResponse(eq(savedComment), eq(userId))).thenReturn(commentResponse)

        // When
        val result = commentService.addComment(request, userId)

        // Then
        assertNotNull(result)
        assertEquals(2L, result.commentId)
        assertEquals("새로운 댓글", result.content)
        verify(commentRepository).save(any<Comment>())
        verify(coverRepository).save(eq(testCover))
    }

    @Test
    @DisplayName("addComment - 대댓글 추가 성공")
    fun testAddReply() {
        // Given
        val parentCommentId = 1L
        val request = CreateServiceCommentRequest(
            content = "대댓글",
            coverId = 1L,
            parentCommentId = parentCommentId,
            userId = 3L,
        )
        val userId = 3L
        val replyComment = Comment(
            content = "대댓글",
            cover = testCover,
            userId = userId,
            parentCommentId = parentCommentId
        ).apply { id = 3L }
        val commentResponse = CommentResponse(
            commentId = 3L,
            content = "대댓글",
            coverId = 1L,
            userId = userId,
            parentCommentId = parentCommentId,
            likeCount = 0,
            isLiked = false
        )

        whenever(coverRepository.findByIdOrNull(eq(1L))).thenReturn(testCover)
        whenever(commentRepository.findByIdOrNull(eq(parentCommentId))).thenReturn(testComment)
        whenever(commentRepository.save(any<Comment>())).thenReturn(replyComment)
        whenever(commentResponseBuilder.buildCommentResponse(eq(replyComment), eq(userId))).thenReturn(commentResponse)

        // When
        val result = commentService.addComment(request, userId)

        // Then
        assertNotNull(result)
        assertEquals(3L, result.commentId)
        assertEquals(parentCommentId, result.parentCommentId)
        verify(commentRepository).save(any<Comment>())
    }

    @Test
    @DisplayName("updateComment - 댓글 수정 성공")
    fun testUpdateComment() {
        // Given
        val commentId = 1L
        val userId = 2L
        val request = UpdateServiceCommentRequest(
            content = "수정된 댓글"
        )
        val updatedComment = Comment(
            content = "수정된 댓글",
            cover = testCover,
            userId = userId,
            parentCommentId = null
        ).apply { id = commentId }
        val commentResponse = CommentResponse(
            commentId = commentId,
            content = "수정된 댓글",
            coverId = 1L,
            userId = userId,
            likeCount = 0,
            isLiked = false
        )

        whenever(commentRepository.findByIdOrNull(eq(commentId))).thenReturn(testComment)
        whenever(commentRepository.save(any<Comment>())).thenReturn(updatedComment)
        whenever(commentResponseBuilder.buildCommentResponse(eq(updatedComment), eq(userId))).thenReturn(commentResponse)

        // When
        val result = commentService.updateComment(commentId, request, userId)

        // Then
        assertNotNull(result)
        assertEquals("수정된 댓글", result.content)
        verify(commentRepository).save(any<Comment>())
    }

    @Test
    @DisplayName("deleteComment - 댓글 삭제 성공")
    fun testDeleteComment() {
        // Given
        val commentId = 1L
        val userId = 2L

        whenever(commentRepository.findByIdOrNull(eq(commentId))).thenReturn(testComment)

        // When
        commentService.deleteComment(commentId, userId)

        // Then
        verify(commentRepository).delete(eq(testComment))
        verify(coverRepository).save(eq(testCover))
    }

    @Test
    @DisplayName("getCommentsByCoverId - 커버의 댓글 조회")
    fun testGetCommentsByCoverId() {
        // Given
        val coverId = 1L
        val userId = 3L
        val comments = listOf(testComment)
        val commentResponse = CommentResponse(
            commentId = 1L,
            content = "좋은 커버곡입니다!",
            coverId = coverId,
            userId = 2L,
            likeCount = 2,
            isLiked = false
        )

        whenever(coverRepository.findByIdOrNull(eq(coverId))).thenReturn(testCover)
        whenever(commentRepository.findAllByCoverId(eq(coverId))).thenReturn(comments)
        whenever(commentResponseBuilder.buildCommentResponse(eq(testComment), eq(userId))).thenReturn(commentResponse)

        // When
        val result = commentService.getCommentsByCoverId(coverId, userId)

        // Then
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(commentResponse.commentId, result[0].commentId)
        verify(commentRepository).findAllByCoverId(eq(coverId))
    }

    @Test
    @DisplayName("getCommentsByUserId - 사용자의 댓글 조회")
    fun testGetCommentsByUserId() {
        // Given
        val userId = 2L
        val comments = listOf(testComment)
        val commentResponse = CommentResponse(
            commentId = 1L,
            content = "좋은 커버곡입니다!",
            coverId = 1L,
            userId = userId,
            likeCount = 1,
            isLiked = true
        )

        whenever(commentRepository.findAllByUserId(eq(userId))).thenReturn(comments)
        whenever(commentResponseBuilder.buildCommentResponse(eq(testComment), eq(userId))).thenReturn(commentResponse)

        // When
        val result = commentService.getCommentsByUserId(userId)

        // Then
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(commentResponse.commentId, result[0].commentId)
        verify(commentRepository).findAllByUserId(eq(userId))
    }
}

