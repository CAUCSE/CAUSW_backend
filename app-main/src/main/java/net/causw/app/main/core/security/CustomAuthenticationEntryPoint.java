package net.causw.app.main.core.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.exception.GlobalErrorCode;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.UnauthorizedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

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

		// v2 API인지 확인
		String requestPath = request.getRequestURI();
		if (requestPath != null && requestPath.startsWith("/api/v2/")) {
			setV2Response(response);
		} else {
			setV1Response(response, errorCode, message);
		}
	}

	private void setV1Response(
		HttpServletResponse response,
		ErrorCode errorCode,
		String message) throws IOException {
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

	private void setV2Response(
		HttpServletResponse response
	) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");

		ApiResponse<String> apiResponse = ApiResponse.error(AuthErrorCode.INVALID_TOKEN);
		String body = objectMapper.writeValueAsString(apiResponse);

		response.getWriter().println(body);
	}
}
