package net.causw.app.main.domain.user.terms.api.v2.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record TermsAgreementRequestDto(
	@NotEmpty
	@Schema(description = "동의할 약관 ID 목록") List<String> termsIds) {
}
