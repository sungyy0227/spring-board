package spring.board.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import spring.board.security.CustomUserDetails;

@ControllerAdvice
public class LoginMemberModelAdvice {

    @ModelAttribute("loginMember")
    public CustomUserDetails loginMember(@AuthenticationPrincipal CustomUserDetails loginMember){
        return loginMember;
    }
}
