package net.causw.app.main.domain.user.account.api.v2.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import net.causw.app.main.domain.user.account.api.v1.dto.UserFcmTokenResponseDto;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserFcmTokenRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserPasswordUpdateRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserRegistrationRequest;
import net.causw.app.main.domain.user.account.service.UserAccountService;
import net.causw.app.main.domain.user.account.service.UserNotificationService;
import net.causw.app.main.domain.user.account.service.dto.request.UserPasswordUpdateCommand;
import net.causw.app.main.domain.user.auth.api.v2.dto.AuthDtoMapper;
import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

@RestController("UserControllerV2")
@RequiredArgsConstructor
@RequestMapping("/api/v2/users")
@Tag(name = "User v2", description = "계정관리 API")
public class UserController {

	private final UserNotificationService userNotificationService;
	private final UserAccountService userAccountService;
	private final AuthDtoMapper authDtoMapper;

	@PostMapping("/fcm")
	@Operation(summary = "fcm 토큰 등록 API", description = "유저와 fcm 토큰을 매핑한다.")
	public ApiResponse<UserFcmTokenResponseDto> createFcmToken(
		@CookieValue(name = "refresh_token", required = false) String refreshToken,
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody() UserFcmTokenRequest body) {
		if (refreshToken == null) {
			throw AuthErrorCode.REFRESH_TOKEN_MISSING.toBaseException();
		}
		return ApiResponse
			.success(userNotificationService.createFcmToken(userDetails.getUserId(), body.fcmToken(), refreshToken));
	}

	@GetMapping("/fcm")
	@Operation(summary = "fcm 토큰 조회 API", description = "유저에게 등록된 fcm 토큰을 조회한다.")
	public ApiResponse<UserFcmTokenResponseDto> findFcmToken(@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(userNotificationService.findFcmTokenByUser(userDetails.getUserId()));
	}

	@PatchMapping("/me/registration")
	@Operation(summary = "소셜로그인 이후 사용자 정보 및 약관 동의 입력 API", description = "GUEST 상태의 유저에게 가입에 필요한 정보를 추가로 받고 AWAIT 상태로 변경한다.")
	public ApiResponse<AuthResponse> submitRegistration(
		@CookieValue(name = "refresh_token", required = false) String refreshToken,
		@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody UserRegistrationRequest body) {
		if (refreshToken == null) {
			throw AuthErrorCode.REFRESH_TOKEN_MISSING.toBaseException();
		}
		AuthResult dto = userAccountService.completeRegistration(userDetails.getUserId(), body.nickname(),
			body.phoneNumber(), body.name(), refreshToken);
		return ApiResponse.success(authDtoMapper.toAuthResponse(dto));
	}

	@GetMapping("/check-nickname")
	@Operation(summary = "닉네임 중복 체크 API", description = "닉네임이 중복인지 확인합니다.")
	public ApiResponse<Void> checkNicknameDuplication(
		@RequestParam String nickname) {
		userAccountService.checkNicknameDuplication(nickname);
		return ApiResponse.success();
	}

	@GetMapping("/check-phone")
	@Operation(summary = "전화번호 중복 체크 API", description = "전화번호가 중복인지 확인합니다.")
	public ApiResponse<Void> checkPhoneNumDuplication(
		@RequestParam String phoneNumber) {
		userAccountService.checkPhoneNumDuplication(phoneNumber);
		return ApiResponse.success();
	}

	@PatchMapping("/me/password")
	@Operation(summary = "비밀번호 재설정 API", description = "현재 비밀번호를 확인하고 새 비밀번호로 변경합니다.")
	public ApiResponse<Void> updatePassword(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody UserPasswordUpdateRequest body) {
		userAccountService.updatePassword(
			userDetails.getUserId(),
			UserPasswordUpdateCommand.from(body));
		return ApiResponse.success();
	}

}
