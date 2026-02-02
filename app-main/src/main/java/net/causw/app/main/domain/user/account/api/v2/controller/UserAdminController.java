package net.causw.app.main.domain.user.account.api.v2.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import net.causw.app.main.domain.user.account.api.v2.dto.request.UserListRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserDetailResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserListItemResponse;
import net.causw.app.main.domain.user.account.api.v2.mapper.UserDetailMapper;
import net.causw.app.main.domain.user.account.api.v2.mapper.UserListMapper;
import net.causw.app.main.domain.user.account.service.UserAdminService;
import net.causw.app.main.shared.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/users")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
public class UserAdminController {

	private final UserAdminService userAdminService;
	private final UserListMapper userListMapper;
	private final UserDetailMapper userDetailMapper;

	@GetMapping
	public ApiResponse<Page<UserListItemResponse>> getUsers(
		@ModelAttribute @Validated UserListRequest request) {
		// page/size 안 보내면 기본값
		int page = request.page() != null ? request.page() : 0;
		int size = request.size() != null ? request.size() : 10;

		PageRequest pageRequest = PageRequest.of(page, size);

		Page<UserListItemResponse> response = userAdminService
			.getUserList(userListMapper.toCondition(request), pageRequest)
			.map(userListMapper::toResponse);

		return ApiResponse.success(response);
	}

	@GetMapping("/{userId}")
	public ApiResponse<UserDetailResponse> getUserDetail(
		@PathVariable String userId) {
		var userDetail = userAdminService.getUserDetail(userId);
		return ApiResponse.success(userDetailMapper.toResponse(userDetail));
	}
}
