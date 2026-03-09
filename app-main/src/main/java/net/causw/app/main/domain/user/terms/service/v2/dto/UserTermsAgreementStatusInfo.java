package net.causw.app.main.domain.user.terms.service.v2.dto;

import java.util.List;

public record UserTermsAgreementStatusInfo(
	List<AgreedTermsInfo> agreedTerms,
	List<UnagreedTermsInfo> unagreedTerms,
	boolean hasAllRequiredAgreements) {
}
