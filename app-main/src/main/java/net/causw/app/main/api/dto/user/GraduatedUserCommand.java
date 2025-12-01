package net.causw.app.main.api.dto.user;

import net.causw.app.main.domain.user.account.enums.user.Department;

public record GraduatedUserCommand(
	String email,
	String name,
	String studentId,
	Integer admissionYear,
	Integer graduationYear,
	String nickname,
	Department department,
	String phoneNumber
) {
}
