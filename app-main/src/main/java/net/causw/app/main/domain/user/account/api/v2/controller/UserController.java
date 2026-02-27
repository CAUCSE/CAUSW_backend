package net.causw.app.main.domain.user.account.api.v2.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.user.account.api.v1.dto.UserFcmTokenResponseDto;
import net.causw.app.main.domain.user.account.api.v2.dto.request.AdmissionCreateRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserFcmTokenRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserNicknameUpdateRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserRegistrationRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.response.AdmissionResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.AdmissionStateResponse;
import net.causw.app.main.domain.user.account.api.v2.mapper.AdmissionDtoMapper;
import net.causw.app.main.domain.user.account.service.AdmissionService;
import net.causw.app.main.domain.user.account.service.UserAccountService;
import net.causw.app.main.domain.user.account.service.UserNotificationService;
import net.causw.app.main.domain.user.account.service.dto.request.AdmissionResult;
import net.causw.app.main.domain.user.auth.api.v2.dto.AuthDtoMapper;
import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;
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
@Tag(name = "User Public v2", description = "일반 사용자를 위한 사용자 정보 조회 및 수정 API")
public class UserController {

	private final UserNotificationService userNotificationService;
	private final UserAccountService userAccountService;
	private final AuthDtoMapper authDtoMapper;
	private final AdmissionService admissionService;
	private final AdmissionDtoMapper admissionDtoMapper;

	// ── 재학정보 인증 ──

	@Operation(summary = "재학정보 인증 신청 V2", description = "회원가입 후 재학정보 인증을 신청합니다. "
		+ "이름, 학과, 입학년도, 학번, 재학분류와 증빙서류 이미지를 제출합니다. "
		+ "관리자 승인 후 서비스 이용이 가능합니다.")
	@PostMapping(value = "/me/admission", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<AdmissionResponse> createAdmission(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestPart(value = "request") @Valid AdmissionCreateRequest request,
		@RequestPart(value = "attachImages") List<MultipartFile> attachImages) {

		AdmissionResult result = admissionService.createAdmission(
			userDetails.getUser(),
			admissionDtoMapper.toCreateCommand(request),
			attachImages);

		return ApiResponse.success(admissionDtoMapper.toResponse(result));
	}

	@Operation(summary = "내 인증 신청 상태 조회 V2", description = "현재 로그인한 사용자의 재학정보 인증 신청 상태를 조회합니다. "
		+ "(AWAIT: 승인 대기, ACTIVE: 승인 완료, REJECT: 거부)")
	@GetMapping("/me/admission/state")
	public ApiResponse<AdmissionStateResponse> getMyAdmissionState(
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		return ApiResponse.success(
			admissionDtoMapper.toStateResponse(
				admissionService.getAdmissionState(userDetails.getUser())));
	}

	// ── FCM ──

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

	// ── ONBOARDING ──

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

	@PutMapping("/me/nickname")
	@Operation(summary = "닉네임 변경 API", description = "현재 로그인한 사용자의 닉네임을 변경합니다. 현재 닉네임과 동일하거나 중복된 닉네임은 변경할 수 없습니다.")
	public ApiResponse<Void> updateNickname(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody UserNicknameUpdateRequest body) {
		userAccountService.updateNickname(userDetails.getUserId(), body.nickname());
		return ApiResponse.success();
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

}
