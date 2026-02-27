package net.causw.app.main.domain.user.terms.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import net.causw.app.main.domain.user.terms.entity.Terms;

public record TermsResponse(
	@Schema(description = "제목") String title,
	@Schema(description = "시행일") String effectiveDate,
	@Schema(description = "최종 개정일") String lastRevisedDate,
	@Schema(description = "내용") String content
) {
	public static TermsResponse from(Terms terms) {
		return new TermsResponse(terms.getTitle(), terms.getEffectiveDate(), terms.getLastRevisedDate(), terms.getContent());
	}
}
