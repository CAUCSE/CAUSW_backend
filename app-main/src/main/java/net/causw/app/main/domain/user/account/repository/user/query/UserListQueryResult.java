package net.causw.app.main.domain.user.account.repository.user.query;

import java.time.LocalDateTime;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.UserState;

import com.querydsl.core.annotations.QueryProjection;

public record UserListQueryResult(
	String id,
	String name,
	String email,
	String studentId,
	Integer admissionYear,
	Department department,
	UserState state,
	AcademicStatus academicStatus,
	LocalDateTime createdAt) {

	@QueryProjection
	public UserListQueryResult {
	}
}
