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
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException {

		// Front의 Request Header가 Authorization: undefined 시 JWT 재발급 요청을 위한 에러 코드를 반환합니다.
		ErrorCode errorCode = ErrorCode.INVALID_JWT;
		String message = MessageUtil.INVALID_TOKEN;

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
