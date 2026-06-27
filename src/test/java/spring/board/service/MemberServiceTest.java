package spring.board.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import spring.board.domain.Member;
import spring.board.domain.Role;
import spring.board.domain.Status;
import spring.board.dto.SignupForm;
import spring.board.dto.SignupValidationError;
import spring.board.repository.MemberRepository;
import spring.board.repository.PostRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class MemberServiceTest {
    @Autowired
    PostService postService;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("정상적으로 회원가입이 됐는지")
    void signupSuccess() {
        //given
        SignupForm signupForm = new SignupForm();
        signupForm.setLoginId("testId");
        signupForm.setNickname("testNickname");
        signupForm.setPassword("test1234");

        //when
        memberService.signup(signupForm);

        //then
        Member savedMember = memberRepository.findByLoginId("testId").orElseThrow();
        assertThat(savedMember.getLoginId()).isEqualTo("testId");
        assertThat(savedMember.getNickname()).isEqualTo("testNickname");
        assertThat(savedMember.getRole()).isEqualTo(Role.USER);
        assertThat(savedMember.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(savedMember.getPassword()).isNotEqualTo("test1234");
        assertThat(passwordEncoder.matches("test1234", savedMember.getPassword())).isTrue();
    }

    @Test
    @DisplayName("이미 존재하는 로그인 아이디로 회원가입하면 실패한다")
    void signupDuplicateLoginIdFails(){
        SignupForm signupForm=new SignupForm();
        signupForm.setLoginId("testId");
        signupForm.setNickname("Nickname1");
        signupForm.setPassword("123456");
        memberService.signup(signupForm);

        SignupForm signupForm2 = new SignupForm();
        signupForm2.setLoginId("testId");
        signupForm2.setNickname("Nickname2");
        signupForm2.setPassword("123456");

        List<SignupValidationError> errors = memberService.signup(signupForm2);

        assertThat(errors).extracting(SignupValidationError::getErrorCode).contains("duplicate.loginId");

        assertThat(memberRepository.findByNickname("testNickname2")).isEmpty();

    }

    @Test
    @DisplayName("이미 존재하는 닉네임으로 회원가입하면 실패한다")
    void signupDuplicateNicknameFails(){
        SignupForm signupForm1 =new SignupForm();
        signupForm1.setLoginId("testId1");
        signupForm1.setNickname("dupnick");
        signupForm1.setPassword("123456");
        memberService.signup(signupForm1);

        SignupForm signupForm2 =new SignupForm();
        signupForm2.setLoginId("testId2");
        signupForm2.setNickname("dupnick");
        signupForm2.setPassword("123456");

        List<SignupValidationError> errors = memberService.signup(signupForm2);

        assertThat(errors).extracting(SignupValidationError::getErrorCode).contains("duplicate.nickname");
        assertThat(memberRepository.findByLoginId("testId2")).isEmpty();
    }

    @Test
    @DisplayName("유효하지 않은 로그인 아이디로 회원가입하면 실패한다")
    void signupInvalidLoginIdFails(){
        SignupForm signupForm = new SignupForm();
        signupForm.setLoginId("test");
        signupForm.setPassword("123456");
        signupForm.setNickname("test");

        List<SignupValidationError> errors = memberService.signup(signupForm);

        assertThat(errors).extracting(SignupValidationError::getErrorCode).contains("invalid.loginId");
        assertThat(memberRepository.findByNickname("test")).isEmpty();
    }

    @Test
    @DisplayName("유효하지 않은 비밀번호로 회원가입하면 실패한다")
    void signupInvalidPasswordFails(){
        SignupForm signupForm = new SignupForm();
        signupForm.setLoginId("testId");
        signupForm.setPassword("1234");
        signupForm.setNickname("test");

        List<SignupValidationError> errors = memberService.signup(signupForm);

        assertThat(errors).extracting(SignupValidationError::getErrorCode).contains("invalid.password");
        assertThat(memberRepository.findByNickname("test")).isEmpty();
    }

    @Test
    @DisplayName("유효하지 않은 닉네임으로 회원가입하면 실패한다")
    void signupInvalidNicknameFails(){
        SignupForm signupForm = new SignupForm();
        signupForm.setLoginId("testId");
        signupForm.setPassword("123456");
        signupForm.setNickname("test-nickname"); //invalid.nickname

        List<SignupValidationError> errors = memberService.signup(signupForm);

        assertThat(errors).extracting(SignupValidationError::getErrorCode).contains("invalid.nickname");
        assertThat(memberRepository.findByLoginId("testId")).isEmpty();
    }

    @Test
    @DisplayName("회원 탈퇴에 성공한다")
    void withdrawSucceeds(){
        Member member = new Member();
        member.setLoginId("testId");
        member.setNickname("tester");
        member.setPassword(passwordEncoder.encode("123456"));
        member.setRole(Role.USER);
        member.setStatus(Status.ACTIVE);
        memberRepository.save(member);

        memberService.withdraw("123456",member.getId());

        assertThat(memberRepository.findByLoginId("testId").orElseThrow().getStatus()).isEqualTo(Status.WITHDRAWN);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 회원 탈퇴에 실패한다")
    void withdrawFailsWithWrongPassword(){
        Member member = new Member();
        member.setLoginId("testId");
        member.setNickname("tester");
        member.setPassword(passwordEncoder.encode("123456"));
        member.setRole(Role.USER);
        member.setStatus(Status.ACTIVE);
        memberRepository.save(member);

        assertThatThrownBy(() -> memberService.withdraw("wrongPassword", member.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 올바르지 않습니다.");

        Member savedMember = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(savedMember.getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    @DisplayName("일반 회원의 권한을 관리자로 변경한다")
    void changeUserRoleToAdmin(){
        Member member = createMember("test", "tester", Role.USER);
        Long id = memberRepository.save(member).getId();

        memberService.grantAdmin(id);

        assertThat(memberRepository.findById(id).orElseThrow().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("관리자의 권한을 일반 회원으로 변경한다")
    void removeAdminChangesRoleToUser(){
        Member member = createMember("test", "tester", Role.ADMIN);
        Long id = memberRepository.save(member).getId();

        memberService.removeAdmin(id);

        assertThat(memberRepository.findById(id).orElseThrow().getRole()).isEqualTo(Role.USER);
    }

    private Member createMember(String loginId,String nickname ,Role role) {
        Member member = new Member();
        member.setLoginId(loginId);
        member.setPassword("testPassword");
        member.setNickname(nickname);
        member.setRole(role);
        return member;
    }
}

