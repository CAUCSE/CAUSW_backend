package net.causw.app.main.domain.user.account.api.v2.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import net.causw.app.main.domain.user.account.api.v1.dto.UserFcmTokenResponseDto;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserFcmTokenRequest;
import net.causw.app.main.domain.user.account.service.UserNotificationService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController("UserControllerV2")
@RequiredArgsConstructor
@RequestMapping("/api/v2/users")
@Tag(name = "User v2", description = "계정관리 API")
public class UserController {

	private final UserNotificationService userNotificationService;

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

}
