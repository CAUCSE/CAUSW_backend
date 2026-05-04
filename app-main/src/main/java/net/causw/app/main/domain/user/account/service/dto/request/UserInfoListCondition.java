package net.causw.app.main.domain.user.account.service.dto.request;

import java.util.List;

public record UserInfoListCondition(
	String keyword,
	Integer admissionYearStart,
	Integer admissionYearEnd,
	List<String> academicStatus,
	String sortType) {
}
