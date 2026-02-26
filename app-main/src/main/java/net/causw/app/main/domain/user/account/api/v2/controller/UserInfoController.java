package net.causw.app.main.domain.user.account.api.v2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.user.account.api.v2.dto.request.UserInfoListCondition;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoDetailResponseDto;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoSummaryResponseDto;
import net.causw.app.main.domain.user.account.service.UserInfoService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "동문 수첩 API V2", description = "동문 수첩 API V2")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/users-info")
public class UserInfoController {

	private final UserInfoService userInfoService;

	/**
	 * 동문수첩 프로필 고유 id 값으로 동문 수첩 프로필 세부 정보를 조회하는 API
	 * @param userInfoId 동문 수첩 프로필 고유 id
	 * @return 동문 수첩 프로필 상세 정보
	 */
	@GetMapping(value = "/{userInfoId}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "동문 수첩 프로필 상세 조회", description = "동문 수첩 프로필 상세 정보를 조회합니다.")
	public ApiResponse<UserInfoDetailResponseDto> getUserInfoDetail(
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
	@Operation(summary = "내 동문 수첩 프로필 상세 조회", description = "내 동문 수첩 프로필 상세 정보를 조회합니다.")
	public ApiResponse<UserInfoDetailResponseDto> getMyUserInfoDetail(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(userInfoService.getMyDetailUserInfo(userDetails.getUserId()));
	}

	/**
	 * 동문 수첩 프로필 리스트 조회 및 검색
	 * @param condition 검색어, 필터
	 * @param pageNum 페이징
	 * @return 조회된 동문 수첩 프로필 리스트
	 */
	@GetMapping(value = "/")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "동문 수첩 프로필 리스트 조회 및 검색", description = "검색어 또는 필터를 포함해 동문 수첩 프로필 리스트를 조회합니다.")
	public ApiResponse<PageResponse<UserInfoSummaryResponseDto>> getUserInfoPage(
		@ModelAttribute UserInfoListCondition condition,
		@RequestParam(name = "pageNum", required = false, defaultValue = "0") Integer pageNum) {
		return ApiResponse.success(PageResponse.from(userInfoService.getUserInfoPage(condition, pageNum)));
	}
}
