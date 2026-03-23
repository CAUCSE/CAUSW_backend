package net.causw.app.main.domain.user.terms.service.v2;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.terms.repository.TermsRepository;
import net.causw.app.main.domain.user.terms.service.v2.dto.TermsInfo;
import net.causw.app.main.shared.exception.errorcode.TermsErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {

	private final TermsRepository termsRepository;

	public List<TermsInfo> getTerms() {
		List<TermsInfo> result = termsRepository.findLatestVersionPerType()
			.stream()
			.map(TermsInfo::from)
			.toList();
		if (result.isEmpty()) {
			throw TermsErrorCode.TERMS_NOT_FOUND.toBaseException();
		}
		return result;
	}
}
