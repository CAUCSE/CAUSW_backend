package net.causw.app.main.domain.user.terms.api.v2.dto.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import net.causw.app.main.domain.user.terms.entity.Terms;

public record TermsResponseDto(
	@Schema(description = "제목") String title,
	@Schema(description = "시행일") LocalDate effectiveDate,
	@Schema(description = "최종 개정일") LocalDate lastRevisedDate,
	@Schema(description = "내용") String content
) {
	public static TermsResponseDto from(Terms terms) {
		return new TermsResponseDto(terms.getTitle(), terms.getEffectiveDate(), terms.getLastRevisedDate(), terms.getContent());
	}
}
