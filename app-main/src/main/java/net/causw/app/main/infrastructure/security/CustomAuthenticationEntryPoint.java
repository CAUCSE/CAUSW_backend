package net.causw.app.main.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.UnauthorizedException;
import net.causw.global.constant.MessageUtil;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        //디폴트 오류처리 설정
        ErrorCode errorCode = ErrorCode.API_NOT_ACCESSIBLE;
        String message = MessageUtil.API_NOT_ACCESSIBLE;

        UnauthorizedException exception = (UnauthorizedException) request.getAttribute("exception");

        if (exception != null) {
            errorCode = exception.getErrorCode();
            message = exception.getMessage();
        }

        setResponse(response, errorCode, message);
    }

    private void setResponse(
            HttpServletResponse response,
            ErrorCode errorCode,
            String message
    ) throws IOException {
        response.setContentType("service/json;charset=UTF-8");
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
