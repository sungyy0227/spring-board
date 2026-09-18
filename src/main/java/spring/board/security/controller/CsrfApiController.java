package spring.board.security.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.board.security.dto.CsrfTokenResponse;

@RestController
@RequestMapping("/api/v1/csrf")
public class CsrfApiController {

    @GetMapping
    public CsrfTokenResponse getCsrfToken(CsrfToken csrfToken) {
        return CsrfTokenResponse.from(csrfToken);
    }
}
