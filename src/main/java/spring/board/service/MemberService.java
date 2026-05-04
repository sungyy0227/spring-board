package spring.board.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import spring.board.domain.Role;
import spring.board.dto.MemberDto;
import spring.board.domain.Member;
import spring.board.repository.MemberRepository;
import spring.board.repository.PostRepository;

@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostRepository postRepository;

    MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, PostRepository postRepository){
        this.passwordEncoder=passwordEncoder;
        this.memberRepository=memberRepository;
        this.postRepository = postRepository;
    }

    public void deleteAccount(Long id){
        memberRepository.deleteById(id);
    }

    public void signup(MemberDto memberDto){
        if(memberRepository.existsByLoginId(memberDto.getLoginId())){
            throw new IllegalArgumentException("이미 사용중인 아이디 입니다.");
        }

        if(memberRepository.existsByNickname(memberDto.getNickname())){
            throw new IllegalArgumentException("이미 사용중인 닉네임 입니다.");
        }   

        Member member=new Member();
        member.setLoginId(memberDto.getLoginId());
        member.setPassword(passwordEncoder.encode(memberDto.getPassword()));
        member.setNickname(memberDto.getNickname());
        member.setRole(Role.USER);

        memberRepository.save(member);
    }

    public void resetAllMember(){
        memberRepository.deleteAllNative();
        memberRepository.resetId();
    }

    public Member login(String loginId, String rawPassword){
        Member member=memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));
        if(!passwordEncoder.matches(rawPassword, member.getPassword())){
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        return member;
    }

    public Member findByLoginId(String keyword){
        return memberRepository.findByLoginId(keyword)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    public Member findByNickname(String keyword){
        return memberRepository.findByNickname(keyword)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    public Member findById(Long id){
        return memberRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    public void grantAdmin(Long id) {
        Member member=memberRepository.findById(id).orElseThrow();
        member.setRole(Role.ADMIN);
    }

    public void removeAdmin(Long id){
        Member member=memberRepository.findById(id).orElseThrow();
        member.setRole(Role.USER); //TODO: 권한을 제거해도 세션이 있는 정보는 새로고침이 안됨
    }


    //관리자용 서비스
    public Member findMember(String keyword,String mode){
        Member member=null;
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }
        if (mode == null || mode.isBlank()) {
            throw new IllegalArgumentException("검색 조건을 선택해주세요.");
        }
        if("loginId".equals(mode)){
            return member = memberRepository.findByLoginId(keyword).orElseThrow(() -> new IllegalArgumentException("해당 아이디를 가진 유저가 존재하지 않습니다."));
            }
        else if("nickname".equals(mode)){
            return member = memberRepository.findByNickname(keyword).orElseThrow(() -> new IllegalArgumentException("해당 닉네임을 가진 유저가 존재하지 않습니다."));
        }

        throw new IllegalArgumentException("올바르지 않은 검색 조건입니다.");
    }
}
