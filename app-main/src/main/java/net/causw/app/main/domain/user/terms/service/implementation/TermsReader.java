package net.causw.app.main.domain.user.terms.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.terms.entity.Terms;
import net.causw.app.main.domain.user.terms.repository.TermsRepository;
import net.causw.app.main.shared.exception.errorcode.TermsErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TermsReader {

	private final TermsRepository termsRepository;

	public List<Terms> findLatestVersionPerType() {
		List<Terms> result = termsRepository.findLatestVersionPerType();
		if (result.isEmpty()) {
			throw TermsErrorCode.TERMS_NOT_FOUND.toBaseException();
		}
		return result;
	}

	public List<Terms> findAllById(List<String> ids) {
		return termsRepository.findAllById(ids);
	}
}
