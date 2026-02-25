package net.causw.app.main.domain.user.terms.service.v2;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.user.terms.api.v2.dto.response.TermsResponse;

@Service
public class TermsService {

	public TermsResponse getTerms() {
		return TermsResponse.of("이용약관", "");
	}
}
