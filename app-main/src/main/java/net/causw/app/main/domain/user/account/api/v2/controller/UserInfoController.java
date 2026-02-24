package net.causw.app.main.domain.user.account.api.v2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoDetailResponseDto;
import net.causw.app.main.domain.user.account.service.UserInfoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "User-Info API V2", description = "동문 수첩 API V2")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/users-info")
public class UserInfoController {

	private final UserInfoService userInfoService;

	/**
	 *  동문수첩 프로필 고유 id 값으로 프로필 세부 정보를 조회하는 API
	 * @param userInfoId
	 * @return
	 */
	@GetMapping(value = "/{userInfoId}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "동문 수첩 프로필 상세 조회", description = "동문 수첩 프로필 상세 정보를 조회합니다.")
	public UserInfoDetailResponseDto getUserInfoDetail(
		@PathVariable("userInfoId") String userInfoId) {
		return userInfoService.getDetailUserInfo(userInfoId);
	}
}
