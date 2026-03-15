package net.causw.app.main.domain.user.terms.api.v2.dto.response;

import java.util.List;

import net.causw.app.main.domain.user.terms.service.v2.dto.UserTermsAgreementStatusInfo;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserTermsAgreementStatusResponseDto(
	@Schema(description = "동의 완료 약관 목록") List<AgreedTermsResponseDto> agreedTerms,
	@Schema(description = "미동의 약관 목록") List<UnagreedTermsResponseDto> unagreedTerms,
	@Schema(description = "전체 약관 동의 여부") boolean hasAllRequiredAgreements) {

	public static UserTermsAgreementStatusResponseDto from(UserTermsAgreementStatusInfo info) {
		return new UserTermsAgreementStatusResponseDto(
			info.agreedTerms().stream().map(AgreedTermsResponseDto::from).toList(),
			info.unagreedTerms().stream().map(UnagreedTermsResponseDto::from).toList(),
			info.hasAllRequiredAgreements());
	}
}
