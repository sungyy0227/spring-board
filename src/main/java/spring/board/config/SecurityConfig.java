package spring.board.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import spring.board.exception.ApiErrorResponse;
import spring.board.exception.ErrorCode;

import java.io.IOException;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SessionRegistry sessionRegistry,
                                           ObjectMapper objectMapper) throws Exception {

        http
                .csrf(csrf -> {
                })
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/admin", "/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/members/me", "/api/v1/members/me/**").authenticated()
                        .requestMatchers("/api/v1/chat/rooms", "/api/v1/chat/rooms/**").authenticated()
                        .requestMatchers("/ws", "/ws/**").authenticated()
                        .anyRequest().permitAll()
                )
                .sessionManagement(session -> session
                        .maximumSessions(-1)
                        .sessionRegistry(sessionRegistry)
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/api/v1/auth/login")
                        .usernameParameter("loginId")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.NO_CONTENT.value());
                        })
                        .failureHandler((request, response, exception) -> {
                            ErrorCode errorCode = exception instanceof DisabledException
                                    ? ErrorCode.WITHDRAWN_MEMBER
                                    : ErrorCode.LOGIN_FAILED;
                            writeJsonError(response, objectMapper, HttpStatus.UNAUTHORIZED, errorCode);
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.NO_CONTENT.value());
                        })
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                writeJsonError(
                                        response,
                                        objectMapper,
                                        HttpStatus.UNAUTHORIZED,
                                        ErrorCode.AUTHENTICATION_REQUIRED)
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeJsonError(
                                        response,
                                        objectMapper,
                                        HttpStatus.FORBIDDEN,
                                        ErrorCode.ACCESS_DENIED
                                )
                        )
                );
        return http.build();
    }

    private static void writeJsonError(jakarta.servlet.http.HttpServletResponse response,
                                       ObjectMapper objectMapper,
                                       HttpStatus status,
                                       ErrorCode errorCode) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiErrorResponse.from(errorCode));
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
