package net.causw.app.main.domain.user.auth.api.v2.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.user.account.service.v2.dto.UserRegisterDto;
import net.causw.app.main.domain.user.auth.api.v2.dto.AuthDtoMapper;
import net.causw.app.main.domain.user.auth.api.v2.dto.request.EmailLoginRequest;
import net.causw.app.main.domain.user.auth.api.v2.dto.request.EmailSignupRequest;
import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;
import net.causw.app.main.domain.user.auth.service.v2.AuthService;
import net.causw.app.main.domain.user.auth.service.v2.dto.AuthResult;
import net.causw.app.main.shared.dto.ApiResponse;
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
}
