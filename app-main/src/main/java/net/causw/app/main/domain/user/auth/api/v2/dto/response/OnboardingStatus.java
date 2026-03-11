package net.causw.app.main.domain.user.auth.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "온보딩 플로우 분기 상태")
public enum OnboardingStatus {
	@Schema(description = "약관 동의 필요")
	TERMS_REQUIRED,

	@Schema(description = "재학 인증 필요")
	ACADEMIC_CERTIFICATION_REQUIRED,

	@Schema(description = "온보딩 완료")
	ACTIVE
}
