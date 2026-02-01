package net.causw.app.main.domain.user.account.api.v2.controller;

import net.causw.app.main.domain.user.account.api.v2.dto.request.UserListRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserListItemResponse;
import net.causw.app.main.domain.user.account.api.v2.mapper.UserListMapper;
import net.causw.app.main.domain.user.account.service.UserQueryService;

import lombok.RequiredArgsConstructor;

import net.causw.app.main.shared.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/users")
public class UserQueryAdminController {

    private final UserQueryService userQueryService;
    private final UserListMapper userListMapper;

    @GetMapping
    public ApiResponse<Page<UserListItemResponse>> getUsers(
            @ModelAttribute @Validated UserListRequest request
    ) {
        // page/size 안 보내면 기본값
        int page = request.page() != null ? request.page() : 0;
        int size = request.size() != null ? request.size() : 10;

        PageRequest pageRequest = PageRequest.of(page, size);

        Page<UserListItemResponse> response =
                userQueryService
                        .getUserList(userListMapper.toCondition(request), pageRequest)
                        .map(userListMapper::toResponse);

        return ApiResponse.success(response);
    }
}
