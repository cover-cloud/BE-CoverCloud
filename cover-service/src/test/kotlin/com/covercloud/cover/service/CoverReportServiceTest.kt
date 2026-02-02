package com.covercloud.cover.service

import com.covercloud.cover.domain.Cover
import com.covercloud.cover.domain.CoverGenre
import com.covercloud.cover.domain.ReportReason
import com.covercloud.cover.repository.CoverRepository
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
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class CoverReportServiceTest {

    @Mock
    private lateinit var coverRepository: CoverRepository

    @InjectMocks
    private lateinit var coverReportService: CoverReportService

    private lateinit var testCover: Cover

    @BeforeEach
    fun setup() {
        testCover = Cover(
            link = "https://example.com/cover",
            userId = 1L,
            musicId = 1L,
            coverArtist = "Artist",
            coverTitle = "Title",
            coverGenre = CoverGenre.K_POP
        ).apply {
            id = 1L
            isReported = false
        }
    }

    @Test
    @DisplayName("reportCover - 신고 성공")
    fun testReportCoverSuccess() {
        // Given
        val coverId = 1L
        val reason = "INAPPROPRIATE_CONTENT"
        val description = "부적절한 내용"

        whenever(coverRepository.findById(coverId)).thenReturn(Optional.of(testCover))

        // When
        val response = coverReportService.reportCover(coverId, reason, description)

        // Then
        assertTrue(response.isReported)
        assertEquals(ReportReason.INAPPROPRIATE_CONTENT, response.reason)
        assertEquals(description, response.description)
        verify(coverRepository).save(any())
    }

    @Test
    @DisplayName("reportCover - 중복 신고 실패")
    fun testReportCoverDuplicate() {
        // Given
        val coverId = 1L
        testCover.isReported = true
        testCover.reportReason = ReportReason.SPAM

        whenever(coverRepository.findById(coverId)).thenReturn(Optional.of(testCover))

        // When & Then
        assertThrows<IllegalArgumentException> {
            coverReportService.reportCover(coverId, "HARASSMENT", "괴롭힘")
        }.apply {
            assertEquals("You have already reported this cover", message)
        }
    }

    @Test
    @DisplayName("reportCover - 유효하지 않은 신고 사유")
    fun testReportCoverInvalidReason() {
        // Given
        val coverId = 1L
        val invalidReason = "INVALID_REASON"

        whenever(coverRepository.findById(coverId)).thenReturn(Optional.of(testCover))

        // When & Then
        assertThrows<IllegalArgumentException> {
            coverReportService.reportCover(coverId, invalidReason, "설명")
        }.apply {
            assertTrue(message?.contains("Invalid reason") == true)
        }
    }

    @Test
    @DisplayName("reportCover - 커버 미존재")
    fun testReportCoverNotFound() {
        // Given
        val coverId = 999L

        whenever(coverRepository.findById(coverId)).thenReturn(Optional.empty())

        // When & Then
        assertThrows<NotFoundException> {
            coverReportService.reportCover(coverId, "SPAM", "스팸")
        }
    }

    @Test
    @DisplayName("reportCover - 다양한 신고 사유 테스트")
    fun testReportCoverDifferentReasons() {
        // Given
        val coverId = 1L
        val reasons = listOf(
            "INAPPROPRIATE_CONTENT",
            "COPYRIGHT_INFRINGEMENT",
            "HARASSMENT",
            "SPAM",
            "MISINFORMATION",
            "OTHER"
        )

        // When & Then
        for (reason in reasons) {
            val cover = Cover(
                link = "https://example.com/cover",
                userId = 1L,
                musicId = 1L,
                coverArtist = "Artist",
                coverTitle = "Title",
                coverGenre = CoverGenre.K_POP
            ).apply {
                id = coverId
                isReported = false
            }

            whenever(coverRepository.findById(coverId)).thenReturn(Optional.of(cover))

            val response = coverReportService.reportCover(coverId, reason, "설명")

            assertTrue(response.isReported)
            assertEquals(ReportReason.valueOf(reason), response.reason)
        }
    }

    @Test
    @DisplayName("getCoverReportInfo - 신고 정보 조회 성공")
    fun testGetCoverReportInfoSuccess() {
        // Given
        val coverId = 1L
        testCover.isReported = true
        testCover.reportReason = ReportReason.SPAM
        testCover.reportDescription = "스팸 글입니다"

        whenever(coverRepository.findById(coverId)).thenReturn(Optional.of(testCover))

        // When
        val response = coverReportService.getCoverReportInfo(coverId)

        // Then
        assertEquals(coverId, response.coverId)
        assertTrue(response.isReported)
        assertEquals(ReportReason.SPAM, response.reason)
        assertEquals("스팸 글입니다", response.description)
    }

    @Test
    @DisplayName("getCoverReportInfo - 신고되지 않은 커버 조회")
    fun testGetCoverReportInfoNotReported() {
        // Given
        val coverId = 1L
        testCover.isReported = false

        whenever(coverRepository.findById(coverId)).thenReturn(Optional.of(testCover))

        // When
        val response = coverReportService.getCoverReportInfo(coverId)

        // Then
        assertEquals(coverId, response.coverId)
        assertFalse(response.isReported)
        assertNull(response.reason)
        assertNull(response.description)
    }

    @Test
    @DisplayName("getCoverReportInfo - 커버 미존재")
    fun testGetCoverReportInfoNotFound() {
        // Given
        val coverId = 999L

        whenever(coverRepository.findById(coverId)).thenReturn(Optional.empty())

        // When & Then
        assertThrows<NotFoundException> {
            coverReportService.getCoverReportInfo(coverId)
        }
    }
}

