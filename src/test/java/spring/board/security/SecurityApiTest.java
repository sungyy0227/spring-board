package spring.board.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import spring.board.member.domain.Member;
import spring.board.member.domain.Role;
import spring.board.member.domain.Status;
import spring.board.member.repository.MemberRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("비로그인 사용자의 현재 로그인 정보를 JSON으로 반환한다")
    void getAnonymousCurrentUser() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    @DisplayName("React 로그인 요청은 리다이렉트 대신 204를 반환한다")
    void loginForReactReturnsNoContent() throws Exception {
        saveMember("reactLogin", "reactNickname", Role.USER);

        mockMvc.perform(post("/api/v1/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("loginId", "reactLogin")
                        .param("password", "123456")
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(authenticated().withUsername("reactLogin"));
    }

    @Test
    @DisplayName("React 로그인 실패는 오류 JSON과 401을 반환한다")
    void loginFailureForReactReturnsJson() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("loginId", "missing")
                        .param("password", "wrong")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_FAILED"));
    }

    @Test
    @DisplayName("비로그인 사용자는 마이페이지 API에 접근할 수 없다")
    void myPageApiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("일반 회원은 관리자 API에 접근할 수 없다")
    void adminApiRequiresAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/admin/runtime")
                        .with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    private void saveMember(String loginId, String nickname, Role role) {
        Member member = new Member();
        member.setLoginId(loginId);
        member.setNickname(nickname);
        member.setPassword(passwordEncoder.encode("123456"));
        member.setRole(role);
        member.setStatus(Status.ACTIVE);
        memberRepository.save(member);
    }
}
