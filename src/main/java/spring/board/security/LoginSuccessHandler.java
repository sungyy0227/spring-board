package spring.board.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import spring.board.dto.SessionMember;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        SessionMember sessionMember = new SessionMember(
                userDetails.getId(),
                userDetails.getLoginId(),
                userDetails.getNickname(),
                userDetails.getRole()
        );

        HttpSession session = request.getSession(true);
        session.setAttribute("loginMember", sessionMember);

        response.sendRedirect("/");
    }
}