package net.causw.app.main.dto.user;

import net.causw.app.main.domain.model.enums.user.Department;

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
