package net.causw.app.main.domain.user.terms.service.v2;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.terms.entity.Terms;
import net.causw.app.main.domain.user.terms.entity.UserTermsAgreement;
import net.causw.app.main.domain.user.terms.service.implementation.TermsReader;
import net.causw.app.main.domain.user.terms.service.implementation.UserTermsAgreementReader;
import net.causw.app.main.domain.user.terms.service.implementation.UserTermsAgreementWriter;
import net.causw.app.main.domain.user.terms.service.v2.dto.AgreedTermsInfo;
import net.causw.app.main.domain.user.terms.service.v2.dto.TermsInfo;
import net.causw.app.main.domain.user.terms.service.v2.dto.UnagreedTermsInfo;
import net.causw.app.main.domain.user.terms.service.v2.dto.UserTermsAgreementStatusInfo;
import net.causw.app.main.shared.exception.errorcode.TermsErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {

	private final TermsReader termsReader;
	private final UserTermsAgreementReader userTermsAgreementReader;
	private final UserTermsAgreementWriter userTermsAgreementWriter;

	public List<TermsInfo> getTerms() {
		return termsReader.findLatestVersionPerType()
			.stream()
			.map(TermsInfo::from)
			.toList();
	}

	public UserTermsAgreementStatusInfo getAgreementStatus(User user) {
		List<Terms> latestTerms = termsReader.findLatestVersionPerType();

		Set<String> latestTermsIds = latestTerms.stream()
			.map(Terms::getId)
			.collect(Collectors.toSet());

		List<UserTermsAgreement> userAgreements = userTermsAgreementReader.findByUser(user)
			.stream()
			.filter(uta -> latestTermsIds.contains(uta.getTerms().getId()))
			.toList();

		Set<String> agreedTermsIds = userAgreements.stream()
			.map(uta -> uta.getTerms().getId())
			.collect(Collectors.toSet());

		List<AgreedTermsInfo> agreedTerms = userAgreements.stream()
			.map(uta -> new AgreedTermsInfo(
				uta.getTerms().getType(),
				uta.getTerms().getVersion(),
				uta.getAgreedAt()))
			.toList();

		List<UnagreedTermsInfo> unagreedTerms = latestTerms.stream()
			.filter(t -> !agreedTermsIds.contains(t.getId()))
			.map(UnagreedTermsInfo::from)
			.toList();

		boolean hasAllRequiredAgreements = agreedTermsIds.containsAll(latestTermsIds);

		return UserTermsAgreementStatusInfo.of(agreedTerms, unagreedTerms, hasAllRequiredAgreements);
	}

	@Transactional
	public void agreeToTerms(User user, List<String> termsIds) {
		List<Terms> latestTerms = termsReader.findLatestVersionPerType();

		Set<String> latestTermsIds = latestTerms.stream()
			.map(Terms::getId)
			.collect(Collectors.toSet());

		if (!Set.copyOf(termsIds).containsAll(latestTermsIds)) {
			throw TermsErrorCode.NOT_ALL_TERMS_AGREED.toBaseException();
		}

		Set<String> alreadyAgreedIds = userTermsAgreementReader.findByUserAndTermsIdIn(user, termsIds)
			.stream()
			.map(uta -> uta.getTerms().getId())
			.collect(Collectors.toSet());

		List<Terms> termsToAgree = termsReader.findAllById(termsIds);
		List<UserTermsAgreement> newAgreements = termsToAgree.stream()
			.filter(terms -> !alreadyAgreedIds.contains(terms.getId()))
			.map(terms -> UserTermsAgreement.of(user, terms))
			.toList();

		userTermsAgreementWriter.saveAll(newAgreements);
	}
}
