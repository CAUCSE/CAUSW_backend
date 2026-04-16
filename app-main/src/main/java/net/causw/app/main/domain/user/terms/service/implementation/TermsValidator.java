package net.causw.app.main.domain.user.terms.service.implementation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.terms.entity.Terms;
import net.causw.app.main.shared.exception.errorcode.TermsErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TermsValidator {

	private final TermsReader termsReader;

	public void validateForAgreement(List<String> termsIds) {
		validateTermsIdsExist(termsIds);
		validateAllRequiredLatestTermsAgreed(termsIds);
	}

	private void validateTermsIdsExist(List<String> termsIds) {
		Set<String> distinctIds = Set.copyOf(termsIds);
		Set<String> foundIds = termsReader.findAllById(List.copyOf(distinctIds))
			.stream()
			.map(Terms::getId)
			.collect(Collectors.toSet());

		if (!foundIds.equals(distinctIds)) {
			throw TermsErrorCode.TERMS_NOT_FOUND.toBaseException();
		}
	}

	private void validateAllRequiredLatestTermsAgreed(List<String> termsIds) {
		Set<String> requiredLatestTermsIds = termsReader.findLatestPerTypeIfRequired()
			.stream()
			.map(Terms::getId)
			.collect(Collectors.toSet());

		if (!Set.copyOf(termsIds).containsAll(requiredLatestTermsIds)) {
			throw TermsErrorCode.NOT_ALL_REQUIRED_TERMS_AGREED.toBaseException();
		}
	}
}
