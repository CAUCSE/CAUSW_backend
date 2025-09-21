package net.causw.app.main.domain.model.enums.user;

import java.util.Arrays;

import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Department {

	DEPT_OF_AI("AI학과"),
	SW_SCHOOL("소프트웨어학부"),
	SCHOOL_OF_CSE("컴퓨터공학부"),
	DEPT_OF_CSE("컴퓨터공학과"),
	CS_DEPT("전자계산학과");

	private final String name;

	public static Department of(String name) {
		return Arrays.stream(Department.values())
			.filter(dept -> dept.name.equals(name))
			.findFirst()
			.orElseThrow(() -> new BadRequestException(
				ErrorCode.INVALID_REQUEST_DEPARTMENT,
				String.format("name '%s' is invalid : not supported", name)
			));
	}
}
