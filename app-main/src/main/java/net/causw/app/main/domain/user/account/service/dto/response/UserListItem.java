package net.causw.app.main.domain.user.account.service.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.repository.user.query.UserListQueryResult;

public record UserListItem(
	String id,
	String name,
	String email,
	String studentId,
	Integer admissionYear,
	Department department,
	UserState state,
	AcademicStatus academicStatus,
	LocalDateTime createdAt) {

	public static UserListItem from(UserListQueryResult queryResult) {
		return new UserListItem(
			queryResult.id(),
			queryResult.name(),
			queryResult.email(),
			queryResult.studentId(),
			queryResult.admissionYear(),
			queryResult.department(),
			queryResult.state(),
			queryResult.academicStatus(),
			queryResult.createdAt());
	}
}
