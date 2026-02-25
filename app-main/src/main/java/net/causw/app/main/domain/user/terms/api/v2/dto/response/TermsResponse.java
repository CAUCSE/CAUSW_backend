package net.causw.app.main.domain.user.terms.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record TermsResponse(
	@Schema(description = "이용약관 제목") String title,
	@Schema(description = "이용약관 내용") String content
) {
	public static TermsResponse of(String title, String content) {
		return new TermsResponse(title, content);
	}
}
