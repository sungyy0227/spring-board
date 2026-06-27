package spring.board.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import spring.board.domain.Member;
import spring.board.domain.Role;
import spring.board.domain.Status;
import spring.board.repository.MemberRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SecurityTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("올바른 로그인 정보로 로그인에 성공한다")
    void loginSuccess() throws Exception {
        Member member = new Member();
        member.setLoginId("testId");
        member.setPassword(passwordEncoder.encode("123456"));
        member.setRole(Role.USER);
        member.setStatus(Status.ACTIVE);
        member.setNickname("tester");
        memberRepository.save(member);

        mockMvc.perform(post("/login")
                .param("loginId","testId")
                .param("password","123456")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("testId"));
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인에 실패한다")
    void loginFailsWithWrongPassword() throws Exception{
        Member member = new Member();
        member.setLoginId("testId");
        member.setPassword(passwordEncoder.encode("123456"));
        member.setRole(Role.USER);
        member.setStatus(Status.ACTIVE);
        member.setNickname("tester");
        memberRepository.save(member);

        mockMvc.perform(post("/login")
                .param("loginId","testId")
                .param("password","wrongPassword")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("탈퇴한 회원은 탈퇴 안내와 함께 로그인에 실패한다")
    void withdrawnMemberLoginFailsWithWithdrawnMessage() throws Exception {
        Member member = new Member();
        member.setLoginId("testId");
        member.setPassword(passwordEncoder.encode("123456"));
        member.setRole(Role.USER);
        member.setStatus(Status.WITHDRAWN);
        member.setNickname("tester");
        memberRepository.save(member);

        mockMvc.perform(post("/login")
                        .param("loginId","testId")
                        .param("password","123456")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=withdrawn"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("관리자 페이지는 관리자 권한이 필요하다")
    void adminPageRequiresAdminAuthority() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("일반 유저는 관리자 페이지에 접속할 수 없다")
    void userCannotAccessAdminPage() throws Exception {
        mockMvc.perform(get("/admin")
                .with(user("uesr").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("관리자 유저는 관리자 페이지에 접속할 수 있다")
    void adminCanAccessAdminPage() throws Exception{
        mockMvc.perform(get("/admin")
                        .with(user("user").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"));
    }
}
