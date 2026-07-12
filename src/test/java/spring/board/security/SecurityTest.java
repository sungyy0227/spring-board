package spring.board.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import spring.board.domain.Member;
import spring.board.domain.Role;
import spring.board.domain.Status;
import spring.board.repository.MemberRepository;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Autowired
    SessionRegistry sessionRegistry;

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

    @Test
    @DisplayName("ADMIN 권한을 해제하면 대상 회원의 로그인 세션이 만료된다")
    void removeAdminExpiresTargetUserSessions() throws Exception {
        Member targetAdmin = saveMember("targetAdmin", "targetAdminNickname", Role.ADMIN);
        saveMember("actingAdmin", "actingAdminNickname", Role.ADMIN);

        MockHttpSession targetSession = loginAndGetSession("targetAdmin", "123456");
        MockHttpSession actingAdminSession = loginAndGetSession("actingAdmin", "123456");

        assertSessionIsNotExpired(targetSession);

        mockMvc.perform(post("/admin/members/{id}/remove-admin", targetAdmin.getId())
                        .session(actingAdminSession)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/members/" + targetAdmin.getId()));

        Member changedMember = memberRepository.findById(targetAdmin.getId()).orElseThrow();
        assertThat(changedMember.getRole()).isEqualTo(Role.USER);
        assertSessionIsExpired(targetSession);
        assertSessionIsNotExpired(actingAdminSession);
    }

    @Test
    @DisplayName("ADMIN 권한을 부여하면 대상 회원의 로그인 세션이 만료된다")
    void grantAdminExpiresTargetUserSessions() throws Exception {
        Member targetUser = saveMember("targetUser", "targetUserNickname", Role.USER);
        saveMember("grantingAdmin", "grantingAdminNickname", Role.ADMIN);

        MockHttpSession targetSession = loginAndGetSession("targetUser", "123456");
        MockHttpSession actingAdminSession = loginAndGetSession("grantingAdmin", "123456");

        assertSessionIsNotExpired(targetSession);

        mockMvc.perform(post("/admin/members/{id}/grant-admin", targetUser.getId())
                        .session(actingAdminSession)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/members/" + targetUser.getId()));

        Member changedMember = memberRepository.findById(targetUser.getId()).orElseThrow();
        assertThat(changedMember.getRole()).isEqualTo(Role.ADMIN);
        assertSessionIsExpired(targetSession);
        assertSessionIsNotExpired(actingAdminSession);
    }

    private Member saveMember(String loginId, String nickname, Role role) {
        Member member = new Member();
        member.setLoginId(loginId);
        member.setPassword(passwordEncoder.encode("123456"));
        member.setRole(role);
        member.setStatus(Status.ACTIVE);
        member.setNickname(nickname);
        return memberRepository.saveAndFlush(member);
    }

    private MockHttpSession loginAndGetSession(String loginId, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
                        .param("loginId", loginId)
                        .param("password", password)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated().withUsername(loginId))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }

    private void assertSessionIsExpired(MockHttpSession session) {
        SessionInformation sessionInformation = sessionRegistry.getSessionInformation(session.getId());
        assertThat(sessionInformation).isNotNull();
        assertThat(sessionInformation.isExpired()).isTrue();
    }

    private void assertSessionIsNotExpired(MockHttpSession session) {
        SessionInformation sessionInformation = sessionRegistry.getSessionInformation(session.getId());
        assertThat(sessionInformation).isNotNull();
        assertThat(sessionInformation.isExpired()).isFalse();
    }

}
