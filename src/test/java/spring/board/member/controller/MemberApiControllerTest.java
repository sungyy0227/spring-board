package spring.board.member.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberApiControllerTest {
    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("유효한 회원가입 JSON 요청은 회원을 생성한다")
    void signup() throws Exception {
        mockMvc.perform(post("/api/v1/members")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "newmember",
                                  "password": "abc12345",
                                  "nickname": "새회원"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("회원가입 검증 오류는 필드별 JSON으로 반환한다")
    void signupValidationFailure() throws Exception {
        mockMvc.perform(post("/api/v1/members")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "a",
                                  "password": "b",
                                  "nickname": "c"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors.length()").value(3));
    }
}
