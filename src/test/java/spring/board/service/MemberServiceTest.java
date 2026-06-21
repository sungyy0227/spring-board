package spring.board.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import spring.board.domain.Member;
import spring.board.domain.Role;
import spring.board.domain.Status;
import spring.board.dto.MemberDto;
import spring.board.repository.MemberRepository;
import spring.board.repository.PostRepository;

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
    void signupSuccess() {
        //given
        MemberDto memberDto = new MemberDto();
        memberDto.setLoginId("testId");
        memberDto.setNickname("testNickname");
        memberDto.setPassword("1234");

        //when
        memberService.signup(memberDto);

        //then
        Member savedMember = memberRepository.findByLoginId("testId").orElseThrow();
        assertThat(savedMember.getLoginId()).isEqualTo("testId");
        assertThat(savedMember.getNickname()).isEqualTo("testNickname");
        assertThat(savedMember.getRole()).isEqualTo(Role.USER);
        assertThat(savedMember.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(savedMember.getPassword()).isNotEqualTo("1234");
        assertThat(passwordEncoder.matches("1234", savedMember.getPassword())).isTrue();
    }
//
//    @Test
//    void withdrawnMemberCannotLogin(){
//        //given
//        Member member=new Member();
//        member.setLoginId("testId");
//        member.setPassword(passwordEncoder.encode("1234"));
//        member.setNickname("testNickname");
//        member.setRole(Role.USER);
//        member.setStatus(Status.WITHDRAWN);
//        memberRepository.save(member);
//
//        //when&then
////        assertThatThrownBy(() -> memberService.login("testId", "1234"))
////                .isInstanceOf(IllegalArgumentException.class)
////                .hasMessage("탈퇴한 회원입니다. 로그인이 불가능합니다.");
//    }
//
//    @Test
//    void signupDuplicateLoginIdFails(){
//        MemberDto memberDto1 =new MemberDto();
//        memberDto1.setLoginId("testId");
//        memberDto1.setNickname("testNickname1");
//        memberDto1.setPassword("1234");
//        memberService.signup(memberDto1);
//
//        MemberDto memberDto2 = new MemberDto();
//        memberDto2.setLoginId("testId");
//        memberDto2.setNickname("testNickname2");
//        memberDto2.setPassword("1234");
//
//        assertThatThrownBy(() -> memberService.signup(memberDto2))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessage("이미 사용중인 아이디 입니다.");
//    }
//
//    @Test
//    void signupDuplicateNicknameFails(){
//        MemberDto memberDto1 =new MemberDto();
//        memberDto1.setLoginId("testId1");
//        memberDto1.setNickname("testNickname");
//        memberDto1.setPassword("1234");
//        memberService.signup(memberDto1);
//
//        MemberDto memberDto2 = new MemberDto();
//        memberDto2.setLoginId("testId2");
//        memberDto2.setNickname("testNickname");
//        memberDto2.setPassword("1234");
//
//        assertThatThrownBy(() -> memberService.signup(memberDto2))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessage("이미 사용중인 닉네임 입니다.");
//    }
//
//    @Test
//    void loginFailsWithWrongPassword(){
//        MemberDto memberDto =new MemberDto();
//        memberDto.setLoginId("testId1");
//        memberDto.setNickname("testNickname");
//        memberDto.setPassword("1234");
//        memberService.signup(memberDto);
//
////                시큐리티로 전환후 memberService.login 메소드 안씀
////                테스트코드 다시 작성 예정
////        assertThatThrownBy(() -> memberService.login("testId1", "wrongPassword"))
////                .isInstanceOf(IllegalArgumentException.class)
////                .hasMessage("아이디 또는 비밀번호가 올바르지 않습니다.");
//    }
//
//    @Test
//    void loginFailsWithUnknownLoginId(){
//        MemberDto memberDto =new MemberDto();
//        memberDto.setLoginId("testId1");
//        memberDto.setNickname("testNickname");
//        memberDto.setPassword("1234");
//        memberService.signup(memberDto);
//
////        assertThatThrownBy(() -> memberService.login("wrongId", "1234"))
////                .isInstanceOf(IllegalArgumentException.class)
////                .hasMessage("아이디 또는 비밀번호가 올바르지 않습니다.");
//}
}
