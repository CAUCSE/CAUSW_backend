package net.causw.app.main.domain.user.terms.api.v2.dto.response;

import java.time.LocalDate;

import net.causw.app.main.domain.user.terms.entity.TermsType;
import net.causw.app.main.domain.user.terms.service.v2.dto.UnagreedTermsInfo;

import io.swagger.v3.oas.annotations.media.Schema;

public record UnagreedTermsResponseDto(
	@Schema(description = "약관 ID") String termsId,
	@Schema(description = "제목") String title,
	@Schema(description = "약관 종류") TermsType type,
	@Schema(description = "필수 동의 여부") boolean isRequired,
	@Schema(description = "버전") int version,
	@Schema(description = "시행일") LocalDate effectiveDate,
	@Schema(description = "내용") String content) {

	public static UnagreedTermsResponseDto from(UnagreedTermsInfo info) {
		return new UnagreedTermsResponseDto(
			info.termsId(),
			info.title(),
			info.type(),
			info.isRequired(),
			info.version(),
			info.effectiveDate(),
			info.content());
	}
}
