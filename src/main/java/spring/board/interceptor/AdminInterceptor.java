package spring.board.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import spring.board.controller.SessionMember;

public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        HttpSession session=request.getSession(false);

        if(session==null){
            response.sendRedirect("/");
            return false;
        }
        SessionMember loginMember = (SessionMember) session.getAttribute("loginMember");

        if (loginMember == null) {
            response.sendRedirect("/");
            return false;
        }

        if (!"admin".equals(loginMember.getRole())) {
            response.sendRedirect("/");
            return false;
        }

        return true;

    }

}
