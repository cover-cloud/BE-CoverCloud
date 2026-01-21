package com.covercloud.cover.controller

import com.covercloud.cover.domain.Cover
import com.covercloud.cover.domain.CoverGenre
import com.covercloud.cover.repository.CoverRepository
import com.covercloud.cover.repository.CoverLikeRepository
import com.covercloud.cover.repository.CoverTagRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CoverControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var coverRepository: CoverRepository

    @Autowired
    private lateinit var coverLikeRepository: CoverLikeRepository

    @Autowired
    private lateinit var coverTagRepository: CoverTagRepository

    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setup() {
        // 테스트 데이터 초기화
        coverLikeRepository.deleteAll()
        coverTagRepository.deleteAll()
        coverRepository.deleteAll()

        // 테스트 커버 생성
        val cover1 = Cover(
            userId = 1L,
            coverTitle = "Test Cover 1",
            coverArtist = "Artist 1",
            coverGenre = CoverGenre.K_POP,
            link = "https://example.com/video1",
            musicId = 1L
        )

        val cover2 = Cover(
            userId = 2L,
            coverTitle = "Test Cover 2",
            coverArtist = "Artist 2",
            coverGenre = CoverGenre.POP,
            link = "https://example.com/video2",
            musicId = 2L
        )

        val cover3 = Cover(
            userId = 3L,
            coverTitle = "Test Cover 3",
            coverArtist = "Artist 3",
            coverGenre = CoverGenre.J_POP,
            link = "https://example.com/video3",
            musicId = 3L

        )

        coverRepository.saveAll(listOf(cover1, cover2, cover3))
    }

//    @Test
//    @DisplayName("period 없이 호출 시 전체 기간 데이터 반환")
//    fun testGetTrendingCoversWithoutPeriod() {
//        mockMvc.perform(get("/api/cover/trending"))
//            .andDo(print())
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.success").value(true))
//            .andExpect(jsonPath("$.data.content").isArray)
//            .andExpect(jsonPath("$.data.content.length()").value(3))
//    }
//
//    @Test
//    @DisplayName("period=DAILY로 호출 시 일간 트렌딩 반환")
//    fun testGetTrendingCoversWithDaily() {
//        mockMvc.perform(get("/api/cover/trending")
//            .param("period", "DAILY"))
//            .andDo(print())
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.success").value(true))
//            .andExpect(jsonPath("$.data.content").isArray)
//    }
//
//    @Test
//    @DisplayName("period=WEEKLY로 호출 시 주간 트렌딩 반환")
//    fun testGetTrendingCoversWithWeekly() {
//        mockMvc.perform(get("/api/cover/trending")
//            .param("period", "WEEKLY"))
//            .andDo(print())
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.success").value(true))
//            .andExpect(jsonPath("$.data.content").isArray)
//    }
//
//    @Test
//    @DisplayName("period=MONTHLY로 호출 시 월간 트렌딩 반환")
//    fun testGetTrendingCoversWithMonthly() {
//        mockMvc.perform(get("/api/cover/trending")
//            .param("period", "MONTHLY"))
//            .andDo(print())
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.success").value(true))
//            .andExpect(jsonPath("$.data.content").isArray)
//    }
//
//    @Test
//    @DisplayName("잘못된 period 값으로 호출 시 500 에러 (예외 핸들러 미구현)")
//    fun testGetTrendingCoversWithInvalidPeriod() {
//        mockMvc.perform(get("/api/cover/trending")
//            .param("period", "INVALID"))
//            .andDo(print())
//            .andExpect(status().isInternalServerError)
//    }
//
//    @Test
//    @DisplayName("genre 필터링 없이 호출 시 모든 장르 반환")
//    fun testGetTrendingCoversWithoutGenre() {
//        mockMvc.perform(get("/api/cover/trending"))
//            .andDo(print())
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.success").value(true))
//            .andExpect(jsonPath("$.data.content.length()").value(3))  // ROCK 2개 + HIPHOP 1개
//    }
//
//    @Test
//    @DisplayName("genre=K_POP으로 필터링 시 K_POP 장르만 반환")
//    fun testGetTrendingCoversWithRockGenre() {
//        mockMvc.perform(get("/api/cover/trending")
//            .param("genre", "K_POP"))
//            .andDo(print())
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.success").value(true))
//            .andExpect(jsonPath("$.data.content.length()").value(1))  // K_POP 1개만
//    }
//
//    @Test
//    @DisplayName("genre=POP으로 필터링 시 POP 장르만 반환")
//    fun testGetTrendingCoversWithHiphopGenre() {
//        mockMvc.perform(get("/api/cover/trending")
//            .param("genre", "POP"))
//            .andDo(print())
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.success").value(true))
//            .andExpect(jsonPath("$.data.content.length()").value(1))  // POP 1개만
//    }
//
//    @Test
//    @DisplayName("period와 genre를 함께 사용")
//    fun testGetTrendingCoversWithPeriodAndGenre() {
//        mockMvc.perform(get("/api/cover/trending")
//            .param("period", "WEEKLY")
//            .param("genre", "K_POP"))
//            .andDo(print())
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.success").value(true))
//            .andExpect(jsonPath("$.data.content.length()").value(1))
//    }
//
//    @Test
//    @DisplayName("페이징 처리 확인 - page=0, size=2")
//    fun testGetTrendingCoversWithPagination() {
//        mockMvc.perform(get("/api/cover/trending")
//            .param("page", "0")
//            .param("size", "2"))
//            .andDo(print())
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.success").value(true))
//            .andExpect(jsonPath("$.data.content.length()").value(2))
//            .andExpect(jsonPath("$.data.totalElements").value(3))
//            .andExpect(jsonPath("$.data.totalPages").value(2))
//    }
//
//    @Test
//    @DisplayName("페이징 처리 확인 - page=1, size=2")
//    fun testGetTrendingCoversWithSecondPage() {
//        mockMvc.perform(get("/api/cover/trending")
//            .param("page", "1")
//            .param("size", "2"))
//            .andDo(print())
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.success").value(true))
//            .andExpect(jsonPath("$.data.content.length()").value(1))  // 마지막 1개
//            .andExpect(jsonPath("$.data.totalElements").value(3))
//    }
//
//    @Test
//    @DisplayName("모든 파라미터 조합 테스트")
//    fun testGetTrendingCoversWithAllParams() {
//        mockMvc.perform(get("/api/cover/trending")
//            .param("period", "WEEKLY")
//            .param("genre", "K_POP")
//            .param("page", "0")
//            .param("size", "10"))
//            .andDo(print())
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.success").value(true))
//            .andExpect(jsonPath("$.data.content").isArray)
//    }

    // ===== POST /trending/search 엔드포인트 테스트 =====

    @Test
    @DisplayName("POST /trending/search - period 없이 호출 시 전체 기간 데이터 반환")
    fun testSearchTrendingWithoutPeriod() {
        val request = """
            {
                "page": 0,
                "size": 20
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/cover/trending/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray)
            .andExpect(jsonPath("$.data.content.length()").value(3))
    }

    @Test
    @DisplayName("POST /trending/search - period=DAILY로 호출")
    fun testSearchTrendingWithDaily() {
        val request = """
            {
                "period": "DAILY",
                "page": 0,
                "size": 20
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/cover/trending/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray)
    }

    @Test
    @DisplayName("POST /trending/search - period=WEEKLY로 호출")
    fun testSearchTrendingWithWeekly() {
        val request = """
            {
                "period": "WEEKLY",
                "page": 0,
                "size": 20
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/cover/trending/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray)
    }

    @Test
    @DisplayName("POST /trending/search - period=MONTHLY로 호출")
    fun testSearchTrendingWithMonthly() {
        val request = """
            {
                "period": "MONTHLY",
                "page": 0,
                "size": 20
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/cover/trending/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray)
    }

    @Test
    @DisplayName("POST /trending/search - 잘못된 period 값으로 호출 시 에러")
    fun testSearchTrendingWithInvalidPeriod() {
        val request = """
            {
                "period": "INVALID",
                "page": 0,
                "size": 20
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/cover/trending/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andDo(print())
            .andExpect(status().isInternalServerError)
    }

    @Test
    @DisplayName("POST /trending/search - 단일 장르 필터링")
    fun testSearchTrendingWithSingleGenre() {
        val request = """
            {
                "genres": ["K_POP"],
                "page": 0,
                "size": 20
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/cover/trending/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(1))
    }

    @Test
    @DisplayName("POST /trending/search - 복수 장르 필터링")
    fun testSearchTrendingWithMultipleGenres() {
        val request = """
            {
                "genres": ["K_POP", "POP"],
                "page": 0,
                "size": 20
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/cover/trending/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(2))
    }

    @Test
    @DisplayName("POST /trending/search - period와 단일 장르 조합")
    fun testSearchTrendingWithPeriodAndSingleGenre() {
        val request = """
            {
                "period": "WEEKLY",
                "genres": ["K_POP"],
                "page": 0,
                "size": 20
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/cover/trending/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(1))
    }

    @Test
    @DisplayName("POST /trending/search - period와 복수 장르 조합")
    fun testSearchTrendingWithPeriodAndMultipleGenres() {
        val request = """
            {
                "period": "DAILY",
                "genres": ["K_POP", "POP", "J_POP"],
                "page": 0,
                "size": 20
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/cover/trending/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(3))
    }

    @Test
    @DisplayName("POST /trending/search - 페이징 처리 확인 (page=0, size=2)")
    fun testSearchTrendingWithPagination() {
        val request = """
            {
                "page": 0,
                "size": 2
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/cover/trending/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(2))
            .andExpect(jsonPath("$.data.totalElements").value(3))
            .andExpect(jsonPath("$.data.totalPages").value(2))
    }

    @Test
    @DisplayName("POST /trending/search - 페이징 처리 확인 (page=1, size=2)")
    fun testSearchTrendingWithSecondPage() {
        val request = """
            {
                "page": 1,
                "size": 2
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/cover/trending/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.totalElements").value(3))
    }

    @Test
    @DisplayName("POST /trending/search - 모든 파라미터 조합 테스트")
    fun testSearchTrendingWithAllParams() {
        val request = """
            {
                "period": "MONTHLY",
                "genres": ["K_POP", "POP"],
                "page": 0,
                "size": 10
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/cover/trending/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray)
            .andExpect(jsonPath("$.data.content.length()").value(2))
    }

    @Test
    @DisplayName("POST /trending/search - 빈 genres 배열로 호출")
    fun testSearchTrendingWithEmptyGenres() {
        val request = """
            {
                "genres": [],
                "page": 0,
                "size": 20
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/cover/trending/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(3))
    }

    @Test
    @DisplayName("POST /trending/search - 최소 요청 본문 (기본값 사용)")
    fun testSearchTrendingWithMinimalRequest() {
        val request = "{}"

        mockMvc.perform(
            post("/api/cover/trending/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray)
    }

    @Test
    @DisplayName("GET /search/title - 제목으로 검색 성공")
    fun testSearchCoversByTitle() {
        mockMvc.perform(
            get("/api/cover/search/title")
                .param("title", "Test Cover 1")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray)
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.content[0].coverTitle").value("Test Cover 1"))
    }

    @Test
    @DisplayName("GET /search/title - 제목 부분 검색")
    fun testSearchCoversByTitlePartialMatch() {
        mockMvc.perform(
            get("/api/cover/search/title")
                .param("title", "Test")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(3))
    }

    @Test
    @DisplayName("GET /search/title - 검색 결과 없음")
    fun testSearchCoversByTitleNoResult() {
        mockMvc.perform(
            get("/api/cover/search/title")
                .param("title", "NonExistent")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(0))
            .andExpect(jsonPath("$.data.totalElements").value(0))
    }

    @Test
    @DisplayName("GET /search/title - 페이징 처리")
    fun testSearchCoversByTitleWithPagination() {
        mockMvc.perform(
            get("/api/cover/search/title")
                .param("title", "Test")
                .param("page", "0")
                .param("size", "2")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(2))
            .andExpect(jsonPath("$.data.totalElements").value(3))
            .andExpect(jsonPath("$.data.totalPages").value(2))
            .andExpect(jsonPath("$.data.isFirst").value(true))
    }

    @Test
    @DisplayName("GET /search/title - 정렬 내림차순")
    fun testSearchCoversByTitleWithDescSort() {
        mockMvc.perform(
            get("/api/cover/search/title")
                .param("title", "Test")
                .param("sortBy", "createdAt")
                .param("sortDirection", "DESC")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(3))
    }

    @Test
    @DisplayName("GET /search/title - 정렬 오름차순")
    fun testSearchCoversByTitleWithAscSort() {
        mockMvc.perform(
            get("/api/cover/search/title")
                .param("title", "Test")
                .param("sortBy", "createdAt")
                .param("sortDirection", "ASC")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(3))
    }

    @Test
    @DisplayName("GET /search/tags - 태그로 검색")
    fun testSearchCoversByTags() {
        mockMvc.perform(
            get("/api/cover/search/tags")
                .param("tags", "test")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray)
    }

    @Test
    @DisplayName("GET /search/tags - 검색 결과 없음")
    fun testSearchCoversByTagsNoResult() {
        mockMvc.perform(
            get("/api/cover/search/tags")
                .param("tags", "nonexistent")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(0))
    }
}