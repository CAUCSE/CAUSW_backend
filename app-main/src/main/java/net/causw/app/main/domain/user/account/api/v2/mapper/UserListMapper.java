package net.causw.app.main.domain.user.account.api.v2.mapper;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.api.v2.dto.request.UserListRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserListItemResponse;
import net.causw.app.main.domain.user.account.service.dto.request.UserListCondition;
import net.causw.app.main.domain.user.account.service.dto.response.UserListItem;

@Component
public class UserListMapper {

	public UserListCondition toCondition(UserListRequest request) {
		return new UserListCondition(
			request.keyword(),
			request.state(),
			request.academicStatus(),
			request.department());
	}

	public UserListItemResponse toResponse(UserListItem dto) {
		return new UserListItemResponse(
			dto.id(),
			dto.name(),
			dto.studentId(),
			dto.department(),
			dto.state(),
			dto.academicStatus(),
			dto.createdAt());
	}
}
