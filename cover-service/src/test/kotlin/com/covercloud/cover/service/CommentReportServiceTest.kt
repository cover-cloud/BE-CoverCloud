package com.covercloud.cover.service

import com.covercloud.cover.domain.Comment
import com.covercloud.cover.domain.Cover
import com.covercloud.cover.domain.CoverGenre
import com.covercloud.cover.repository.CommentRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.crossstore.ChangeSetPersister
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class CommentReportServiceTest {

    @Mock
    private lateinit var commentRepository: CommentRepository

    @InjectMocks
    private lateinit var commentReportService: CommentReportService

    private lateinit var testCover: Cover
    private lateinit var testComment: Comment

    @BeforeEach
    fun setup() {
        testCover = Cover(
            userId = 1L,
            musicId = 1L,
            link = "https://example.com/1",
            coverTitle = "Test Cover",
            coverArtist = "Test Artist",
            coverGenre = CoverGenre.K_POP,
            likeCount = 0
        ).apply {
            id = 1L
        }

        testComment = Comment(
            content = "Test Comment",
            cover = testCover,
            userId = 1L
        ).apply {
            id = 1L
        }
    }

    @Test
    @DisplayName("reportComment - 댓글 신고 성공")
    fun testReportCommentSuccess() {
        // Given
        val commentId = 1L
        val reason = "SPAM"
        val description = "This is spam"

        whenever(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment))
        whenever(commentRepository.save(any())).thenReturn(testComment)

        // When
        val result = commentReportService.reportComment(commentId, reason, description)

        // Then
        assertNotNull(result)
        assertEquals(commentId, result.commentId)
        assertTrue(result.isReported)
        assertEquals(reason, result.reason)
        assertEquals(description, result.description)
        assertNotNull(result.reportedAt)

        assertTrue(testComment.isReported)
        assertEquals(reason, testComment.reportReason)
        assertEquals(description, testComment.reportDescription)
        assertNotNull(testComment.reportedAt)

        verify(commentRepository).save(testComment)
    }

    @Test
    @DisplayName("reportComment - 유효하지 않은 신고 사유로 실패")
    fun testReportCommentInvalidReason() {
        // Given
        val commentId = 1L
        val invalidReason = "INVALID_REASON"
        val description = "Test description"

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            commentReportService.reportComment(commentId, invalidReason, description)
        }

        assertTrue(exception.message!!.contains("Invalid reason"))
    }

    @Test
    @DisplayName("reportComment - 댓글을 찾을 수 없음")
    fun testReportCommentNotFound() {
        // Given
        val commentId = 999L
        val reason = "SPAM"
        val description = "Test description"

        whenever(commentRepository.findById(commentId)).thenReturn(Optional.empty())

        // When & Then
        assertThrows<ChangeSetPersister.NotFoundException> {
            commentReportService.reportComment(commentId, reason, description)
        }
    }

    @Test
    @DisplayName("reportComment - 이미 신고된 댓글")
    fun testReportCommentAlreadyReported() {
        // Given
        val commentId = 1L
        val reason = "SPAM"
        val description = "Test description"

        testComment.isReported = true
        testComment.reportReason = "HARASSMENT"

        whenever(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment))

        // When & Then
        val exception = assertThrows<IllegalStateException> {
            commentReportService.reportComment(commentId, reason, description)
        }

        assertTrue(exception.message!!.contains("already reported"))
    }

    @Test
    @DisplayName("getCommentReportInfo - 신고 정보 조회 성공")
    fun testGetCommentReportInfoSuccess() {
        // Given
        val commentId = 1L
        testComment.isReported = true
        testComment.reportReason = "SPAM"
        testComment.reportDescription = "Test spam"

        whenever(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment))

        // When
        val result = commentReportService.getCommentReportInfo(commentId)

        // Then
        assertNotNull(result)
        assertEquals(commentId, result.commentId)
        assertTrue(result.isReported)
        assertEquals("SPAM", result.reason)
        assertEquals("Test spam", result.description)
    }

    @Test
    @DisplayName("getCommentReportInfo - 신고되지 않은 댓글")
    fun testGetCommentReportInfoNotReported() {
        // Given
        val commentId = 1L
        testComment.isReported = false

        whenever(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment))

        // When
        val result = commentReportService.getCommentReportInfo(commentId)

        // Then
        assertNotNull(result)
        assertEquals(commentId, result.commentId)
        assertFalse(result.isReported)
        assertNull(result.reason)
        assertNull(result.description)
    }

    @Test
    @DisplayName("getCommentReportInfo - 댓글을 찾을 수 없음")
    fun testGetCommentReportInfoNotFound() {
        // Given
        val commentId = 999L

        whenever(commentRepository.findById(commentId)).thenReturn(Optional.empty())

        // When & Then
        assertThrows<ChangeSetPersister.NotFoundException> {
            commentReportService.getCommentReportInfo(commentId)
        }
    }
}

