package net.causw.app.main.domain.user.terms.service.v2;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.causw.app.main.domain.user.terms.api.v2.dto.response.TermsResponseDto;
import net.causw.app.main.domain.user.terms.repository.TermsRepository;
import net.causw.app.main.shared.exception.errorcode.TermsErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {

	private final TermsRepository termsRepository;

	public TermsResponseDto getTerms() {
		return termsRepository.findTopByOrderByCreatedAtDesc()
			.map(TermsResponseDto::from)
			.orElseThrow(TermsErrorCode.TERMS_NOT_FOUND::toBaseException);
	}
}
