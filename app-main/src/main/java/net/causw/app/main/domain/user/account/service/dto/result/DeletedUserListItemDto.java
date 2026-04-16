package net.causw.app.main.domain.user.account.service.dto.result;

import java.time.LocalDateTime;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.repository.user.query.DeletedUserListQueryResult;

public record DeletedUserListItemDto(
	String id,
	String name,
	String email,
	String studentId,
	Integer admissionYear,
	Department department,
	UserState userState,
	AcademicStatus academicStatus,
	LocalDateTime deletedAt,
	String dropReason) {

	public static DeletedUserListItemDto from(DeletedUserListQueryResult queryResult) {
		return new DeletedUserListItemDto(
			queryResult.id(),
			queryResult.name(),
			queryResult.email(),
			queryResult.studentId(),
			queryResult.admissionYear(),
			queryResult.department(),
			queryResult.userState(),
			queryResult.academicStatus(),
			queryResult.deletedAt(),
			queryResult.dropReason()
		);
	}
}
