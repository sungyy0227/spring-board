package spring.board.security.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.board.security.CustomUserDetails;
import spring.board.security.dto.CurrentUserResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthApiController {

    @GetMapping("/me")
    public CurrentUserResponse getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails loginMember
    ) {
        if (loginMember == null) {
            return CurrentUserResponse.anonymous();
        }

        return CurrentUserResponse.from(loginMember);
    }
}
