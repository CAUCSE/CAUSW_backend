package net.causw.app.main.dto.user;

import net.causw.app.main.domain.model.enums.user.Department;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record GraduatedUserRegisterRequestDto(
	@NotBlank
	String name,

	@NotBlank
	String nickname,

	@NotNull
	Integer admissionYear,

	@NotNull
	Integer graduationYear,

	@Email
	@NotBlank
	String email,

	@NotBlank
	String password,

	@NotBlank
	@Pattern(regexp = "^01(?:0|1|[6-9])-(\\d{3}|\\d{4})-\\d{4}$", message = "전화번호 형식에 맞지 않습니다.")
	String phoneNumber,

	@NotBlank
	Department department,

	String studentId
) {
	public GraduatedUserCommand toGraduatedUserCommand() {
		return new GraduatedUserCommand(
			this.email,
			this.name,
			this.studentId,
			this.admissionYear,
			this.graduationYear,
			this.nickname,
			this.department,
			this.phoneNumber
		);
	}
}
