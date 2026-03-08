package net.causw.app.main.domain.user.account.api.v2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.user.account.api.v2.dto.request.UserInfoListRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserInfoUpdateRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoDetailResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoSummaryResponse;
import net.causw.app.main.domain.user.account.api.v2.mapper.UserInfoDtoMapper;
import net.causw.app.main.domain.user.account.service.UserInfoService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "UserInfo Public v2", description = "동문 수첩 API V2")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/users-info")
@PreAuthorize("@security.isActiveUser()")
public class UserInfoController {

	private final UserInfoService userInfoService;
	private final UserInfoDtoMapper userInfoDtoMapper;

	/**
	 * 동문수첩 프로필 고유 id 값으로 동문 수첩 프로필 세부 정보를 조회하는 API
	 * @param userInfoId 동문 수첩 프로필 고유 id
	 * @return 동문 수첩 프로필 상세 정보
	 */
	@GetMapping(value = "/{userInfoId}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "동문 수첩 프로필 상세 조회", description = "동문 수첩 프로필 상세 정보를 조회합니다.")
	public ApiResponse<UserInfoDetailResponse> getUserInfoDetail(
		@PathVariable("userInfoId") String userInfoId) {
		return ApiResponse.success(userInfoService.getDetailUserInfo(userInfoId));
	}

	/**
	 * 사용자 본인의 고유 id 값으로 본인 동문 수첩 프로필 세부 정보를 조회하는 API
	 * @param userDetails 사용자 본인 정보
	 * @return 내 동문 수첩 프로필 상세 정보
	 */
	@GetMapping(value = "/me")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "내 동문 수첩 프로필 상세 조회", description = "내 동문 수첩 프로필 상세 정보를 조회합니다. (아직 생성되지 않은 경우 생성)")
	public ApiResponse<UserInfoDetailResponse> getMyUserInfoDetail(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(userInfoService.getMyDetailUserInfo(userDetails.getUser()));
	}

	/**
	 * 내 동문 수첩 프로필 수정
	 * @param userDetails 사용자 본인 정보
	 * @param request 프로필 수정 요청 DTO
	 * @return 수정된 내 동문 수첩 프로필 상세 정보
	 */
	@PutMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "내 동문 수첩 프로필 업데이트", description = "내 동문 수첩 프로필을 업데이트합니다. (아직 생성되지 않은 경우 생성)")
	public ApiResponse<UserInfoDetailResponse> updateMyUserInfo(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody @Valid UserInfoUpdateRequest request) {
		UserInfoDetailResponse response = userInfoService.updateUserInfo(userInfoDtoMapper.toUpdateCommand(request),
			userDetails.getUser());
		return ApiResponse.success(response);
	}

	/**
	 * 동문 수첩 프로필 리스트 조회 및 검색
	 * @param request 동문 수첩 프로필 리스트 조회 요청 DTO
	 * @param pageNum 페이징
	 * @return 조회된 동문 수첩 프로필 리스트
	 */
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "동문 수첩 프로필 리스트 조회 및 검색", description = "검색어 또는 필터를 포함해 동문 수첩 프로필 리스트를 조회합니다.")
	public ApiResponse<PageResponse<UserInfoSummaryResponse>> getUserInfoPage(
		@ModelAttribute UserInfoListRequest request,
		@RequestParam(name = "pageNum", required = false, defaultValue = "0") Integer pageNum) {
		PageResponse<UserInfoSummaryResponse> response = PageResponse.from(
			userInfoService.getUserInfoPage(userInfoDtoMapper.toListCondition(request), pageNum));
		return ApiResponse.success(response);
	}
}
