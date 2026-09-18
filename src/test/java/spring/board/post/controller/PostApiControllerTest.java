package spring.board.post.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("CSRF 토큰을 JSON으로 조회한다")
    void getCsrfToken() throws Exception {
        mockMvc.perform(get("/api/v1/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.parameterName").value("_csrf"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("게스트가 JSON 요청으로 게시글을 작성한다")
    void createGuestPost() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "API 게시글",
                                  "content": "API로 작성한 내용입니다.",
                                  "poster": "게스트",
                                  "guestPassword": "qwer1234",
                                  "imageIds": []
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId").isNumber());
    }

    @Test
    @DisplayName("필수 입력값이 없으면 API 오류 JSON을 반환한다")
    void createPostFailsWhenTitleIsEmpty() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "content": "내용",
                                  "poster": "게스트",
                                  "guestPassword": "qwer1234",
                                  "imageIds": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POST_TITLE_REQUIRED"))
                .andExpect(jsonPath("$.message").value("제목은 필수입니다."));
    }

    @Test
    @DisplayName("CSRF 토큰이 없는 글 작성 요청은 거부한다")
    void createPostFailsWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
