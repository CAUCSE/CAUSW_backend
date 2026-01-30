package net.causw.app.main.domain.user.account.api.v2.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.user.account.api.v2.dto.request.UserSearchCondition;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserSearchListResponse;
import net.causw.app.main.domain.user.account.api.v2.mapper.UserSearchConditionMapper;
import net.causw.app.main.domain.user.account.api.v2.mapper.UserSearchListMapper;
import net.causw.app.main.domain.user.account.service.UserQueryService;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/users")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
@Tag(name = "관리자 유저 조회 api", description = "관리자 유저 조회 API")
public class UserQueryAdminController {

	private final UserQueryService userQueryService;
	private final UserSearchConditionMapper userSearchConditionMapper;
	private final UserSearchListMapper userSearchListMapper;

	@GetMapping
	public ApiResponse<UserSearchListResponse> searchUsers(
		@ModelAttribute UserSearchCondition userSearchCondition) {
		var result = userQueryService.searchUsers(
			userSearchConditionMapper.toServiceDto(userSearchCondition));
		UserSearchListResponse response = userSearchListMapper.toResponse(result);

		return ApiResponse.success(response);
	}
}
