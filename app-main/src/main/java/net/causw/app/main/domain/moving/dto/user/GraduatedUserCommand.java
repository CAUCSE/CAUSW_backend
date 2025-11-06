package net.causw.app.main.domain.moving.dto.user;

import net.causw.app.main.domain.user.enums.user.Department;

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
