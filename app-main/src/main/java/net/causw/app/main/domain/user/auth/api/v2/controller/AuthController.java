package net.causw.app.main.domain.user.auth.api.v2.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import net.causw.app.main.domain.user.account.service.dto.request.UserRegisterDto;
import net.causw.app.main.domain.user.auth.api.v2.dto.AuthDtoMapper;
import net.causw.app.main.domain.user.auth.api.v2.dto.request.EmailLoginRequest;
import net.causw.app.main.domain.user.auth.api.v2.dto.request.EmailSignupRequest;
import net.causw.app.main.domain.user.auth.api.v2.dto.request.SignOutRequest;
import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;
import net.causw.app.main.domain.user.auth.service.AuthService;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;
import net.causw.app.main.domain.user.auth.service.dto.AuthTokenPair;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.util.AuthorizationExtractor;
import net.causw.global.constant.StaticValue;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth V2", description = "회원가입/로그인 API V2")
@RestController
@RequestMapping("api/v2/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthDtoMapper authDtoMapper;
	private final AuthService authService;

	@Operation(summary = "이메일 회원가입 V2", description = "이메일을 활용하여 사용자 계정을 생성합니다.")
	@PostMapping("/signup")
	public ApiResponse<AuthResponse> emailSignUp(@RequestBody @Valid EmailSignupRequest request) {
		UserRegisterDto dto = UserRegisterDto.from(request);
		AuthResult result = authService.registerEmailUser(dto);
		return ApiResponse.success(authDtoMapper.toAuthResponse(result));
	}

	@Operation(summary = "이메일 로그인 V2", description = "이메일을 활용하여 사용자 계정에 로그인합니다.")
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<AuthResponse>> emailSignIn(@RequestBody @Valid EmailLoginRequest request) {
		AuthResult dto = authService.loginEmailUser(request.email(), request.password());

		// 쿠키로 리프레시토큰 반환
		ResponseCookie cookie = ResponseCookie.from("refresh_token", dto.refreshToken())
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(Duration.ofMillis(StaticValue.JWT_REFRESH_TOKEN_VALID_TIME))
			.sameSite("None")
			.build();

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
			.body(ApiResponse.success(authDtoMapper.toAuthResponse(dto)));
	}

	@Operation(summary = "토큰 재발급 V2", description = "리프레시토큰을 통해 액세스토큰을 재발급 받습니다.")
	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<AuthResponse>> reissue(
		@CookieValue(name = "refresh_token", required = false) String refreshToken,
		@RequestHeader(value = "Authorization", required = false) String authHeader) {
		// CSRF 방어 로직
		AuthorizationExtractor.validate(authHeader);
		AuthResult dto = authService.updateToken(refreshToken);

		// 쿠키로 리프레시토큰 반환
		ResponseCookie cookie = ResponseCookie.from("refresh_token", dto.refreshToken())
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(Duration.ofMillis(StaticValue.JWT_REFRESH_TOKEN_VALID_TIME))
			.sameSite("None")
			.build();

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
			.body(ApiResponse.success(authDtoMapper.toAuthResponse(dto)));
	}

	@Operation(summary = "로그아웃 V2", description = "토큰을 만료시킵니다.")
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<String>> logout(
		@RequestHeader("Authorization") String bearerToken,
		@CookieValue(name = "refresh_token", required = false) String refreshToken,
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody(required = false) SignOutRequest body) {
		String accessToken = AuthorizationExtractor.extract(bearerToken);
		AuthTokenPair tokens = AuthTokenPair.of(accessToken, refreshToken);
		String fcmToken = (body != null) ? body.fcmToken() : null;
		authService.signOut(userDetails.getUserId(), tokens, fcmToken);
		// 쿠키에서 refresh_token 제거
		ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(0)
			.sameSite("None")
			.build();
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
			.body(ApiResponse.success("로그아웃 성공"));
	}
}
