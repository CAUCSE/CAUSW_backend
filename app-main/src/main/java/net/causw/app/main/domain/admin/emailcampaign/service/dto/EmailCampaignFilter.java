package net.causw.app.main.domain.admin.emailcampaign.service.dto;

import java.util.LinkedHashSet;
import java.util.List;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.shared.exception.errorcode.EmailCampaignErrorCode;

public record EmailCampaignFilter(
	List<Integer> admissionYears,
	List<Department> departments,
	List<AcademicStatus> academicStatuses) {

	private static final int MIN_ADMISSION_YEAR = 1900;
	private static final int MAX_ADMISSION_YEAR = 2100;

	public EmailCampaignFilter {
		admissionYears = normalize(admissionYears);
		departments = normalize(departments);
		academicStatuses = normalize(academicStatuses);
		if (admissionYears.stream().anyMatch(EmailCampaignFilter::isInvalidAdmissionYear)) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_INVALID_FILTER.toBaseException();
		}
	}

	private static boolean isInvalidAdmissionYear(Integer year) {
		return year == null || year < MIN_ADMISSION_YEAR || year > MAX_ADMISSION_YEAR;
	}

	private static <T> List<T> normalize(List<T> values) {
		return values == null ? List.of() : List.copyOf(new LinkedHashSet<>(values));
	}
}
