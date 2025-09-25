package net.causw.app.main.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.dto.userInfo.UserInfoResponseDto;
import net.causw.app.main.dto.userInfo.UserInfoSearchConditionDto;
import net.causw.app.main.dto.userInfo.UserInfoSummaryResponseDto;
import net.causw.app.main.dto.userInfo.UserInfoUpdateRequestDto;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;
import net.causw.app.main.service.userInfo.UserInfoService;
import net.causw.app.main.service.userInfo.useCase.command.UpdateUserInfoUseCaseService;
import net.causw.app.main.service.userInfo.useCase.query.GetUserInfoUseCaseService;
import net.causw.app.main.service.userInfo.useCase.query.SearchUserInfoListUseCaseService;
import net.causw.global.exception.BadRequestException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users-info")
@RequiredArgsConstructor
public class UserInfoController {

	private final UserInfoService userInfoService;
	private final SearchUserInfoListUseCaseService searchUserInfoListUseCaseService;
	private final GetUserInfoUseCaseService getUserInfoUseCaseService;
	private final UpdateUserInfoUseCaseService updateUserInfoUseCaseService;

	/**
	 *  사용자 고유 id 값으로 사용자 세부정보를 조회하는 API
	 * @param userId
	 * @return
	 */
	@GetMapping(value = "/{userId}")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "사용자 id로 사용자 세부정보 조회 API")
	public UserInfoResponseDto getUserInfoByUserId(
		@PathVariable("userId") String userId
	) {
		return getUserInfoUseCaseService.execute(userId);
	}

	@GetMapping
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "전체 사용자 리스트 검색 및 조회 API", description = "최근 수정된 순서대로 정렬")
	public Page<UserInfoSummaryResponseDto> searchUserInfos(
		@RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum,
		@ModelAttribute UserInfoSearchConditionDto userInfoSearchCondition
	) {
		return searchUserInfoListUseCaseService.execute(userInfoSearchCondition, pageNum);
	}

	@GetMapping(value = "/me")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "자신의 세부정보 조회 API")
	public UserInfoResponseDto getCurrentUser(
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		return getUserInfoUseCaseService.execute(userDetails.getUserId());
	}

	@PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "자신의 세부정보 갱신 API")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
		@ApiResponse(responseCode = "4001", description = "커리어 날짜가 유효하지 않습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
		@ApiResponse(responseCode = "4002", description = "파일 크기(5MB)가 초과되었습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
	})

	public UserInfoResponseDto updateCurrentUser(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestPart(value = "userInfoUpdateDto") @Valid UserInfoUpdateRequestDto userInfoUpdateDto,
		@RequestPart(value = "profileImage", required = false) MultipartFile profileImage
	) {
		return updateUserInfoUseCaseService.execute(userDetails.getUserId(), userInfoUpdateDto, profileImage);
	}
}