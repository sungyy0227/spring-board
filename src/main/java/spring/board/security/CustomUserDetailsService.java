package spring.board.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import spring.board.domain.Member;
import spring.board.repository.MemberRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;


    public CustomUserDetailsService(MemberRepository memberRepository){
        this.memberRepository = memberRepository;

    }

    @Override
    public UserDetails loadUserByUsername(String loginId) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException(""));

        return new CustomUserDetails(member);
    }
}
