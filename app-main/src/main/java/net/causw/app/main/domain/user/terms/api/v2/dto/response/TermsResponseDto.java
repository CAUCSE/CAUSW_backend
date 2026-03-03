package net.causw.app.main.domain.user.terms.api.v2.dto.response;

import java.time.LocalDate;

import net.causw.app.main.domain.user.terms.entity.Terms;
import net.causw.app.main.domain.user.terms.entity.TermsType;

import io.swagger.v3.oas.annotations.media.Schema;

public record TermsResponseDto(
	@Schema(description = "제목") String title,
	@Schema(description = "약관 종류") TermsType type,
	@Schema(description = "필수 동의 여부") boolean isRequired,
	@Schema(description = "버전") int version,
	@Schema(description = "시행일") LocalDate effectiveDate,
	@Schema(description = "내용") String content) {

	public static TermsResponseDto from(Terms terms) {
		return new TermsResponseDto(
			terms.getTitle(),
			terms.getType(),
			terms.isRequired(),
			terms.getVersion(),
			terms.getEffectiveDate(),
			terms.getContent());
	}
}
