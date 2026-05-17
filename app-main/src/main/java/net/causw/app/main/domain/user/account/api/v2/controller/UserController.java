package net.causw.app.main.domain.user.account.api.v2.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.user.account.api.v1.dto.UserFcmTokenResponseDto;
import net.causw.app.main.domain.user.account.api.v2.dto.request.AdmissionCreateRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.SocialLinkRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UpdateProfileImageRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserFcmTokenRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserNicknameUpdateRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserPasswordUpdateRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserRegistrationRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.response.AdmissionResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.AdmissionStateResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.ProfileImageResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.SocialAccountsResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserMeAccountResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserMeResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserWithdrawResponse;
import net.causw.app.main.domain.user.account.api.v2.mapper.AdmissionDtoMapper;
import net.causw.app.main.domain.user.account.api.v2.mapper.SocialAccountsMapper;
import net.causw.app.main.domain.user.account.api.v2.mapper.UserMeMapper;
import net.causw.app.main.domain.user.account.service.AdmissionService;
import net.causw.app.main.domain.user.account.service.SocialLinkService;
import net.causw.app.main.domain.user.account.service.UserAccountService;
import net.causw.app.main.domain.user.account.service.UserNotificationService;
import net.causw.app.main.domain.user.account.service.UserProfileImageService;
import net.causw.app.main.domain.user.account.service.dto.request.UserPasswordUpdateCommand;
import net.causw.app.main.domain.user.account.service.dto.response.AdmissionResult;
import net.causw.app.main.domain.user.auth.api.v2.dto.AuthDtoMapper;
import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;
import net.causw.app.main.domain.user.auth.api.v2.dto.response.OAuthLinkTokenResponse;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;
import net.causw.app.main.domain.user.auth.service.implementation.OAuthLinkTokenStore;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.util.AuthorizationExtractor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
	private final SocialLinkService socialLinkService;
	private final OAuthLinkTokenStore oAuthLinkTokenStore;
	private final AuthDtoMapper authDtoMapper;
	private final AdmissionService admissionService;
	private final AdmissionDtoMapper admissionDtoMapper;
	private final UserProfileImageService userProfileImageService;
	private final UserMeMapper userMeMapper;
	private final SocialAccountsMapper socialAccountsMapper;

	// ── 내 정보 ──

	@GetMapping("/me")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "내 정보 조회 V2", description = "현재 로그인한 사용자의 기본 정보를 조회합니다. 내정보 메인페이지 진입 시 호출합니다.")
	public ApiResponse<UserMeResponse> getMyProfile(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(
			userMeMapper.toResponse(
				userAccountService.getMyProfile(userDetails.getUserId())));
	}

	@GetMapping("/me/account")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "계정정보 관리 조회 V2", description = "계정정보 관리 페이지 진입 시 호출합니다. 기본 정보 + 전화번호/학번/전공/학과를 반환합니다.")
	public ApiResponse<UserMeAccountResponse> getMyAccountProfile(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(
			userMeMapper.toAccountResponse(
				userAccountService.getMyAccountProfile(userDetails.getUserId())));
	}

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
	@Operation(summary = "fcm 토큰 등록 API", description = "유저와 fcm 토큰을 매핑한다.", security = {
		@SecurityRequirement(name = "refreshBearerAuth")
	})
	public ApiResponse<UserFcmTokenResponseDto> createFcmToken(
		@RequestHeader(value = AuthorizationExtractor.REFRESH_AUTHORIZATION_HEADER, required = false) String refreshAuthHeader,
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody() UserFcmTokenRequest body) {
		AuthorizationExtractor.validateRefresh(refreshAuthHeader);
		String refreshToken = AuthorizationExtractor.extractRefresh(refreshAuthHeader);
		return ApiResponse
			.success(userNotificationService.createFcmToken(userDetails.getUserId(), body.fcmToken(), refreshToken));
	}

	@GetMapping("/fcm")
	@Operation(summary = "fcm 토큰 조회 API", description = "유저에게 등록된 fcm 토큰을 조회한다.")
	public ApiResponse<UserFcmTokenResponseDto> findFcmToken(@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(userNotificationService.findFcmTokenByUser(userDetails.getUserId()));
	}

	@PatchMapping("/me/registration")
	@Operation(summary = "소셜로그인 이후 사용자 정보 및 약관 동의 입력 API", description = "GUEST 상태의 유저에게 가입에 필요한 정보와 필수 약관 동의를 받고 AWAIT 상태로 변경한다.", security = {
		@SecurityRequirement(name = "refreshBearerAuth")
	})
	public ApiResponse<AuthResponse> submitRegistration(
		@RequestHeader(value = AuthorizationExtractor.REFRESH_AUTHORIZATION_HEADER, required = false) String refreshAuthHeader,
		@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody UserRegistrationRequest body) {
		AuthorizationExtractor.validateRefresh(refreshAuthHeader);
		String refreshToken = AuthorizationExtractor.extractRefresh(refreshAuthHeader);
		AuthResult dto = userAccountService.completeRegistration(userDetails.getUserId(), body.nickname(),
			body.phoneNumber(), body.name(), body.agreedTermsIds(), refreshToken);
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

	@PostMapping("/password-change")
	@Operation(summary = "비밀번호 재설정 API", description = "이메일과 현재 비밀번호를 확인하고 새 비밀번호로 변경합니다. "
		+ "비밀번호 찾기 후 임시 비밀번호로 로그인하기 전에도 호출할 수 있습니다.")
	public ApiResponse<Void> updatePassword(
		@Valid @RequestBody UserPasswordUpdateRequest body) {
		userAccountService.updatePassword(UserPasswordUpdateCommand.from(body));
		return ApiResponse.success();
	}

	// ── 프로필 이미지 ──

	@PatchMapping("/me/profile-image/default")
	@Operation(summary = "기본 프로필 이미지 변경 API", description = "프로필 이미지를 기본 이미지(MALE_1, MALE_2, FEMALE_1, FEMALE_2)로 변경합니다. "
		+ "기존 커스텀 이미지가 있는 경우 해당 이미지는 삭제됩니다.")
	public ApiResponse<ProfileImageResponse> updateProfileImageToDefault(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody UpdateProfileImageRequest request) {
		return ApiResponse.success(
			userProfileImageService.updateToDefaultProfileImage(userDetails.getUserId(), request.profileImageType()));
	}

	@PatchMapping(value = "/me/profile-image/custom", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "커스텀 프로필 이미지 변경 API", description = "프로필 이미지를 업로드한 커스텀 이미지로 변경합니다. "
		+ "커스텀 이미지는 1개만 유지되며, 새 이미지로 변경하면 기존 커스텀 이미지는 삭제됩니다.")
	public ApiResponse<ProfileImageResponse> updateProfileImageToCustom(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestPart("image") MultipartFile imageFile) {
		return ApiResponse.success(
			userProfileImageService.updateToCustomProfileImage(userDetails.getUserId(), imageFile));
	}

	// ── 회원 탈퇴 ──
	@DeleteMapping("/me")
	@Operation(summary = "회원 탈퇴 API", description = "현재 로그인한 사용자를 탈퇴 처리합니다. (Soft Delete)", security = {
		@SecurityRequirement(name = "bearerAuth"),
		@SecurityRequirement(name = "refreshBearerAuth")
	})
	public ApiResponse<UserWithdrawResponse> withdraw(
		@Parameter(description = "플랫폼 타입 (애플 로그인 연동 해제 시 정확한 처리를 위해 필요)", example = "ios / web") @RequestHeader(value = "X-Platform-Type", required = false) String platformHint,
		@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
		@RequestHeader(value = AuthorizationExtractor.REFRESH_AUTHORIZATION_HEADER, required = false) String refreshAuthHeader,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		AuthorizationExtractor.validate(authorizationHeader);
		String accessToken = AuthorizationExtractor.extract(authorizationHeader);

		AuthorizationExtractor.validateRefresh(refreshAuthHeader);
		String refreshToken = AuthorizationExtractor.extractRefresh(refreshAuthHeader);

		return ApiResponse.success(
			userAccountService.withdraw(userDetails.getUserId(), accessToken, refreshToken, platformHint));
	}
	// ── 소셜 계정 연동 ──

	@GetMapping("/me/social-accounts")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "소셜 계정 연동 현황 조회 V2", description = "현재 로그인한 사용자의 소셜 계정(Google, Kakao, Apple) 연동 여부를 조회합니다.")
	public ApiResponse<SocialAccountsResponse> getSocialAccounts(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(
			socialAccountsMapper.toResponse(
				socialLinkService.getSocialAccounts(userDetails.getUserId())));
	}

	@PostMapping("/me/social-accounts/native")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "소셜 계정 연동 (Native)", description = "provider 토큰을 검증하여 현재 로그인한 사용자에게 소셜 계정을 연동합니다. "
		+ "카카오는 accessToken, 구글/애플은 idToken을 전달합니다. (네이티브 앱 전용)")
	public ApiResponse<Void> linkSocialAccount(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody SocialLinkRequest body) {
		socialLinkService.linkSocialAccount(
			userDetails.getUserId(), body.provider(), body.accessToken(), body.idToken());
		return ApiResponse.success();
	}

	@PostMapping("/me/social-accounts/{provider}/oauth")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "소셜 계정 연동 초기화 (OAuth)", description = "웹 브라우저에서 OAuth 방식으로 소셜 계정을 연동하기 위한 초기화 API입니다. "
		+ "연동 가능 여부를 사전 검증하고 1회용 링크 토큰을 발급합니다. "
		+ "응답의 linkToken을 /oauth2/authorization/{provider}?linkToken={value} 쿼리 파라미터로 전달하여 소셜 로그인을 진행합니다. (웹 브라우저 전용)")
	public ApiResponse<OAuthLinkTokenResponse> initOAuthLink(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable String provider) {
		socialLinkService.validateLinkable(userDetails.getUserId(), provider);
		String linkToken = oAuthLinkTokenStore.issueToken(userDetails.getUserId());
		return ApiResponse.success(new OAuthLinkTokenResponse(linkToken));
	}

	@DeleteMapping("/me/social-accounts/{provider}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "소셜 계정 연동 해제", description = "현재 로그인한 사용자의 소셜 계정 연동을 해제합니다. "
		+ "비밀번호 없는 계정의 마지막 소셜 계정은 해제할 수 없습니다.")
	public ApiResponse<Void> unlinkSocialAccount(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable String provider) {
		socialLinkService.unlinkSocialAccount(userDetails.getUserId(), provider);
		return ApiResponse.success();
	}
}
