package net.causw.app.main.domain.user.auth.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "온보딩 플로우 분기 상태")
public enum OnboardingStatus {
	@Schema(description = "약관 동의 필요 (v1 유저 대상)")
	TERMS_REQUIRED,

	@Schema(description = "사용자 정보 입력 필요")
	GUEST,

	@Schema(description = "재학 인증 필요")
	ACADEMIC_CERTIFICATION_REQUIRED,

	@Schema(description = "온보딩 완료")
	ACTIVE;

	public static OnboardingStatus resolve(boolean isGuest, boolean hasAllRequiredLatestTerms, boolean isAcademicCertified) {
		if (isGuest) {
			return GUEST;
		}
		if (!isAcademicCertified) {
			return ACADEMIC_CERTIFICATION_REQUIRED;
		}
		if (!hasAllRequiredLatestTerms) {
			return TERMS_REQUIRED;
		}
		return ACTIVE;
	}
}
