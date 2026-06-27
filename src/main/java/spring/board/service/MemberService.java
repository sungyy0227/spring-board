package spring.board.service;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import spring.board.domain.Role;
import spring.board.domain.Status;
import spring.board.dto.SignupForm;
import spring.board.domain.Member;
import spring.board.dto.SignupValidationError;
import spring.board.repository.MemberRepository;
import spring.board.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;

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

    //회원가입, 입력값 검증
    public List<SignupValidationError> signup(SignupForm signupForm){
        List<SignupValidationError> validationErrors = signupValidation(signupForm);
        if(!validationErrors.isEmpty()){
            return validationErrors;
        }
        Member member=new Member();
        member.setLoginId(signupForm.getLoginId());
        member.setPassword(passwordEncoder.encode(signupForm.getPassword()));
        member.setNickname(signupForm.getNickname());
        member.setRole(Role.USER);
        member.setStatus(Status.ACTIVE);
        
        memberRepository.save(member);
        return validationErrors;
    }

    //회원가입 요청시 입력 값 검증
    public List<SignupValidationError> signupValidation(SignupForm signupForm){
        List<SignupValidationError> validationErrors = new ArrayList<>();
        String loginId = signupForm.getLoginId() == null ? "" : signupForm.getLoginId();
        String password = signupForm.getPassword() == null ? "" : signupForm.getPassword();
        String nickname = signupForm.getNickname() == null ? "" : signupForm.getNickname();

        if(!loginId.matches("^[a-zA-Z0-9]{5,20}$")){ //아이디 길이 검사
            validationErrors.add(new SignupValidationError("loginId",
                    "invalid.loginId", "아이디는 영문과 숫자만 사용해서 5~20자로 입력해야 합니다."));
        }
        if(!password.matches("^[A-Za-z0-9]{6,20}$")){ //비밀번호 유효성 검사
            validationErrors.add(new SignupValidationError("password",
                    "invalid.password", "비밀번호는 영문과 숫자만 사용해서 6~20자로 입력해야 합니다."));
        }
        if(memberRepository.existsByLoginId(loginId)){ //아이디 중복 검사
            validationErrors.add(new SignupValidationError("loginId",
                    "duplicate.loginId", "이미 사용 중인 아이디입니다."));
        }
        if(memberRepository.existsByNickname(nickname)){ //닉네임 중복 검사
            validationErrors.add(new SignupValidationError("nickname",
                    "duplicate.nickname", "이미 사용 중인 닉네임입니다."));
        }
        if(!nickname.matches("^[가-힣a-zA-Z0-9_]{2,12}$")){
            validationErrors.add(new SignupValidationError("nickname",
                    "invalid.nickname", "닉네임은 한글, 영문, 숫자, _만 사용해서 2~12자로 입력해야 합니다."));
        }

        return validationErrors;
    }

    public void resetAllMember(){
        memberRepository.deleteAllNative();
        memberRepository.resetId();
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
    public Member findMemberByKeyword(String keyword,String mode){
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

    public void withdraw(String rawPassword,Long id){
        Member member = memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        if(passwordEncoder.matches(rawPassword, member.getPassword())) {
            member.setStatus(Status.WITHDRAWN);
        }
        else{
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

    }
}
