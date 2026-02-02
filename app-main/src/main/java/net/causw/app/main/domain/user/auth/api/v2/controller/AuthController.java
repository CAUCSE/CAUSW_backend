package net.causw.app.main.domain.user.auth.api.v2.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.user.auth.api.v2.dto.request.EmailSignupRequest;
import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;
import net.causw.app.main.domain.user.auth.service.v2.dto.UserRegisterCommand;
import net.causw.app.main.domain.user.auth.service.v2.implementation.RegisterUserUseCase;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth V2", description = "회원가입/로그인 API V2")
@RestController
@RequestMapping("api/v2/auth")
@RequiredArgsConstructor
public class AuthController {

	private final RegisterUserUseCase registerUserUseCase;

	@Operation(summary = "이메일 회원가입 V2", description = "이메일을 활용하여 사용자 계정을 생성합니다.")
	@PostMapping("/signup")
	public ApiResponse<AuthResponse> emailSignUp(@RequestBody @Valid EmailSignupRequest request) {
		UserRegisterCommand command = UserRegisterCommand.from(request);
		return ApiResponse.success(registerUserUseCase.execute(command));
	}
}
