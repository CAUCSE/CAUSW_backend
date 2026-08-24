package net.causw.app.main.domain.user.account.service.dto.request;

import java.util.List;

import net.causw.app.main.domain.user.account.enums.user.Department;

public record UserInfoListCondition(
	String keyword,
	Integer admissionYearStart,
	Integer admissionYearEnd,
	List<String> academicStatus,
	List<Department> department,
	String sortType) {
}
