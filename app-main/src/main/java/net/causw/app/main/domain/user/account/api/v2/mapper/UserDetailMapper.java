package net.causw.app.main.domain.user.account.api.v2.mapper;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.api.v2.dto.response.UserDetailResponse;
import net.causw.app.main.domain.user.account.service.dto.response.UserDetailItem;

@Component
public class UserDetailMapper {

	public UserDetailResponse toResponse(UserDetailItem item) {
		return new UserDetailResponse(
			item.id(),
			item.email(),
			item.name(),
			item.studentId(),
			item.admissionYear(),
			item.roles(),
			item.profileImageUrl(),
			item.state(),
			item.nickname(),
			item.major(),
			item.department(),
			item.academicStatus(),
			item.graduationYear(),
			item.graduationType(),
			item.phoneNumber(),
			item.rejectionOrDropReason(),
			item.createdAt(),
			item.updatedAt());
	}
}
