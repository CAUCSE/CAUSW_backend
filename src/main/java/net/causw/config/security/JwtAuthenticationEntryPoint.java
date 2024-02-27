package net.causw.config.security;

import lombok.extern.slf4j.Slf4j;
import net.causw.domain.exceptions.ErrorCode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ErrorCode errorCode = (ErrorCode) request.getAttribute("exception");

        // API Access without JWT token or invalid JWT
        if (errorCode == null || errorCode == ErrorCode.INVALID_JWT) {
            this.setResponse(response, ErrorCode.INVALID_JWT, "ACCESS TOKEN 만료");
            return;
        }

        // API Access with JWT token includes user data whose role is NONE or state is not ACTIVE
        if (errorCode == ErrorCode.NEED_SIGN_IN) {
            this.setResponse(response, ErrorCode.NEED_SIGN_IN, "다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.");
        }
    }

    private void setResponse(
            HttpServletResponse response,
            ErrorCode errorCode,
            String message
    ) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().println(
                "{" +
                        "\"errorCode\" : \"" + errorCode.getCode() + "\"," +
                        "\"message\" : \"" + message + "\"," +
                        "\"timeStamp\" : \"" + LocalDateTime.now() + "\"" +
                        "}"
        );
    }
}