package com.covercloud.cover.controller

import com.covercloud.cover.domain.Comment
import com.covercloud.cover.domain.Cover
import com.covercloud.cover.domain.CoverGenre
import com.covercloud.cover.repository.CommentRepository
import com.covercloud.cover.repository.CommentLikeRepository
import com.covercloud.cover.repository.CoverRepository
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
class CommentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var commentRepository: CommentRepository

    @Autowired
    private lateinit var commentLikeRepository: CommentLikeRepository

    @Autowired
    private lateinit var coverRepository: CoverRepository

    private val objectMapper = ObjectMapper()
    private lateinit var testCover: Cover
    private lateinit var testComment: Comment

    @BeforeEach
    fun setup() {
        // 테스트 데이터 초기화
        commentLikeRepository.deleteAll()
        commentRepository.deleteAll()
        coverRepository.deleteAll()

        // 테스트 커버 생성
        testCover = Cover(
            userId = 1L,
            coverTitle = "Test Cover",
            coverArtist = "Test Artist",
            coverGenre = CoverGenre.K_POP,
            link = "https://example.com/video1",
            musicId = 1L
        )
        val savedCover = coverRepository.save(testCover)

        // 테스트 댓글 생성
        testComment = Comment(
            content = "좋은 커버곡입니다!",
            cover = savedCover,
            userId = 2L,
            parentCommentId = null
        )
        commentRepository.save(testComment)
    }

    @Test
    @DisplayName("POST /comment/create - 댓글 추가 성공")
    fun testCreateComment() {
        val request = """
            {
                "content": "새로운 댓글",
                "coverId": ${testCover.id}
            }
        """.trimIndent()

        mockMvc.perform(post("/api/cover/comment/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request)
            .header("X-User-Id", "2")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").value("새로운 댓글"))
            .andExpect(jsonPath("$.data.coverId").value(testCover.id))
            .andExpect(jsonPath("$.data.likeCount").value(0))
            .andExpect(jsonPath("$.data.isLiked").value(false))
            .andExpect(jsonPath("$.data.nickname").exists())
            .andExpect(jsonPath("$.data.createdAt").exists())
    }

    @Test
    @DisplayName("POST /comment/update - 댓글 수정 성공")
    fun testUpdateComment() {
        val request = """
            {
                "content": "수정된 댓글"
            }
        """.trimIndent()

        mockMvc.perform(post("/api/cover/comment/update")
            .param("commentId", testComment.id.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(request)
            .header("X-User-Id", "2")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").value("수정된 댓글"))
    }

    @Test
    @DisplayName("POST /comment/delete - 댓글 삭제 성공")
    fun testDeleteComment() {
        mockMvc.perform(post("/api/cover/comment/delete")
            .param("commentId", testComment.id.toString())
            .header("X-User-Id", "2")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Comment deleted successfully"))
    }

    @Test
    @DisplayName("GET /comment/list - 커버의 댓글 목록 조회")
    fun testGetComments() {
        mockMvc.perform(get("/api/cover/comment/list")
            .param("coverId", testCover.id.toString())
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].content").value("좋은 커버곡입니다!"))
            .andExpect(jsonPath("$.data[0].likeCount").value(0))
            .andExpect(jsonPath("$.data[0].isLiked").value(false))
            .andExpect(jsonPath("$.data[0].nickname").exists())
            .andExpect(jsonPath("$.data[0].createdAt").exists())
    }

    @Test
    @DisplayName("GET /comment/my - 사용자의 댓글 목록 조회")
    fun testGetMyComments() {
        mockMvc.perform(get("/api/cover/comment/my")
            .header("X-User-Id", "2")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].userId").value(2))
    }

    @Test
    @DisplayName("POST /comment/like - 댓글 좋아요 추가")
    fun testToggleLikeAdd() {
        mockMvc.perform(post("/api/cover/comment/like")
            .param("commentId", testComment.id.toString())
            .header("X-User-Id", "3")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.likeCount").value(1))
            .andExpect(jsonPath("$.data.isLiked").value(true))
            .andExpect(jsonPath("$.data.nickname").exists())
            .andExpect(jsonPath("$.data.profileImageUrl").exists())
            .andExpect(jsonPath("$.data.createdAt").exists())
    }

    @Test
    @DisplayName("POST /comment/like - 댓글 좋아요 취소")
    fun testToggleLikeRemove() {
        // 먼저 좋아요 추가
        mockMvc.perform(post("/api/cover/comment/like")
            .param("commentId", testComment.id.toString())
            .header("X-User-Id", "3")
        )
            .andExpect(status().isOk)

        // 그 다음 좋아요 취소
        mockMvc.perform(post("/api/cover/comment/like")
            .param("commentId", testComment.id.toString())
            .header("X-User-Id", "3")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.likeCount").value(0))
            .andExpect(jsonPath("$.data.isLiked").value(false))
            .andExpect(jsonPath("$.data.nickname").exists())
            .andExpect(jsonPath("$.data.createdAt").exists())
    }

    @Test
    @DisplayName("POST /comment/like - 여러 사용자의 좋아요")
    fun testToggleLikeMultipleUsers() {
        // 사용자 3이 좋아요
        mockMvc.perform(post("/api/cover/comment/like")
            .param("commentId", testComment.id.toString())
            .header("X-User-Id", "3")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.likeCount").value(1))
            .andExpect(jsonPath("$.data.isLiked").value(true))
            .andExpect(jsonPath("$.data.nickname").exists())

        // 사용자 4도 좋아요
        mockMvc.perform(post("/api/cover/comment/like")
            .param("commentId", testComment.id.toString())
            .header("X-User-Id", "4")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.likeCount").value(2))
            .andExpect(jsonPath("$.data.isLiked").value(true))
            .andExpect(jsonPath("$.data.nickname").exists())
            .andExpect(jsonPath("$.data.createdAt").exists())
    }

    @Test
    @DisplayName("GET /comment/list - 로그인하지 않은 사용자는 isLiked=false")
    fun testGetCommentsWithoutLogin() {
        mockMvc.perform(get("/api/cover/comment/list")
            .param("coverId", testCover.id.toString())
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data[0].isLiked").value(false))
    }

    @Test
    @DisplayName("GET /comment/list - 로그인한 사용자는 isLiked 상태 표시")
    fun testGetCommentsWithLogin() {
        // 먼저 좋아요 추가
        mockMvc.perform(post("/api/cover/comment/like")
            .param("commentId", testComment.id.toString())
            .header("X-User-Id", "5")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.isLiked").value(true))

        // 그 후 댓글 목록 조회
        mockMvc.perform(get("/api/cover/comment/list")
            .param("coverId", testCover.id.toString())
            .header("X-User-Id", "5")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data[0].isLiked").value(true))
            .andExpect(jsonPath("$.data[0].likeCount").value(1))
    }
}

