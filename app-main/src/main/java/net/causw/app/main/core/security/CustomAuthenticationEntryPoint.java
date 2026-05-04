package net.causw.app.main.core.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.exception.BaseResponseCode;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.UnauthorizedException;

import com.fasterxml.jackson.databind.ObjectMapper;

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

		String requestPath = request.getRequestURI();
		boolean isV2 = isV2Path(requestPath);

		if (isV2) {
			AuthErrorCode authErrorCode = mapToAuthErrorCode(errorCode);
			setV2Response(response, authErrorCode);
		} else {
			setV1Response(response, errorCode, message);
		}
	}

	/**
	 * 시큐리티 필터의 v2 체인과 동일한 경로: /api/v2/**, /oauth2/**, /login/oauth2/**
	 */
	private boolean isV2Path(String requestPath) {
		if (requestPath == null) {
			return false;
		}
		return requestPath.startsWith("/api/v2/")
			|| requestPath.startsWith("/oauth2/")
			|| requestPath.startsWith("/login/oauth2/");
	}

	/**
	 * v2 API용: JWT 검증 단계에서 사용하는 v1 ErrorCode를 AuthErrorCode(BaseResponseCode)로 매핑합니다.
	 */
	private AuthErrorCode mapToAuthErrorCode(ErrorCode errorCode) {
		return switch (errorCode) {
			case EXPIRED_JWT -> AuthErrorCode.EXPIRED_TOKEN;
			case INVALID_JWT -> AuthErrorCode.INVALID_TOKEN;
			default -> AuthErrorCode.INVALID_TOKEN;
		};
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
		HttpServletResponse response,
		BaseResponseCode authErrorCode) throws IOException {
		response.setStatus(authErrorCode.getStatus().value());
		response.setContentType("application/json;charset=UTF-8");

		ApiResponse<String> apiResponse = ApiResponse.error(authErrorCode);
		String body = objectMapper.writeValueAsString(apiResponse);

		response.getWriter().println(body);
	}
}
