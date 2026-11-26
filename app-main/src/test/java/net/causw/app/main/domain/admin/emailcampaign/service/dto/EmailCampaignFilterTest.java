package net.causw.app.main.domain.admin.emailcampaign.service.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.EmailCampaignErrorCode;

class EmailCampaignFilterTest {

	@Test
	@DisplayName("필터는 null을 빈 목록으로 바꾸고 중복을 제거한다")
	void normalizeFilter() {
		// when
		EmailCampaignFilter filter = new EmailCampaignFilter(
			List.of(2020, 2020),
			null,
			List.of(AcademicStatus.ENROLLED, AcademicStatus.ENROLLED));

		// then
		assertThat(filter.admissionYears()).containsExactly(2020);
		assertThat(filter.departments()).isEmpty();
		assertThat(filter.academicStatuses()).containsExactly(AcademicStatus.ENROLLED);
	}

	@Test
	@DisplayName("입학연도가 허용 범위를 벗어나면 거부한다")
	void rejectInvalidAdmissionYear() {
		// when & then
		assertThatThrownBy(() -> new EmailCampaignFilter(
			List.of(1899),
			List.of(Department.SCHOOL_OF_SW),
			List.of(AcademicStatus.ENROLLED)))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.extracting(exception -> ((BaseRunTimeV2Exception)exception).getErrorCode())
			.isEqualTo(EmailCampaignErrorCode.EMAIL_CAMPAIGN_INVALID_FILTER);
	}
}
