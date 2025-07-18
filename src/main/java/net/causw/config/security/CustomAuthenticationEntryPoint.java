package net.causw.config.security;

import lombok.extern.slf4j.Slf4j;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.util.MessageUtil;
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
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        // 기본 에러코드 및 메시지 설정
        ErrorCode errorCode = ErrorCode.API_NOT_ACCESSIBLE;
        String message = MessageUtil.API_NOT_ACCESSIBLE;

        Object exceptionAttribute = request.getAttribute("exception");

        if (exceptionAttribute instanceof UnauthorizedException exception) {
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
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String body = """
                {
                    "errorCode" : "%s",
                    "message" : "%s",
                    "timeStamp" : "%s"
                }
                """.formatted(errorCode.getCode(), message, LocalDateTime.now());

        response.getWriter().println(body);
    }
}
