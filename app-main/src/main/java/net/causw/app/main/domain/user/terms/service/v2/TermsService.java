package net.causw.app.main.domain.user.terms.service.v2;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.terms.entity.Terms;
import net.causw.app.main.domain.user.terms.entity.UserTermsAgreement;
import net.causw.app.main.domain.user.terms.repository.TermsRepository;
import net.causw.app.main.domain.user.terms.repository.UserTermsAgreementRepository;
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

	private final TermsRepository termsRepository;
	private final UserTermsAgreementRepository userTermsAgreementRepository;

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

	public UserTermsAgreementStatusInfo getAgreementStatus(User user) {
		List<Terms> latestTerms = termsRepository.findLatestVersionPerType();
		if (latestTerms.isEmpty()) {
			throw TermsErrorCode.TERMS_NOT_FOUND.toBaseException();
		}

		Set<String> latestTermsIds = latestTerms.stream()
			.map(Terms::getId)
			.collect(Collectors.toSet());

		List<UserTermsAgreement> userAgreements = userTermsAgreementRepository.findByUser(user)
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

		return new UserTermsAgreementStatusInfo(agreedTerms, unagreedTerms, hasAllRequiredAgreements);
	}

	@Transactional
	public void agreeToTerms(User user, List<String> termsIds) {
		List<Terms> latestTerms = termsRepository.findLatestVersionPerType();

		Set<String> latestTermsIds = latestTerms.stream()
			.map(Terms::getId)
			.collect(Collectors.toSet());

		if (!Set.copyOf(termsIds).containsAll(latestTermsIds)) {
			throw TermsErrorCode.NOT_ALL_TERMS_AGREED.toBaseException();
		}

		List<Terms> termsToAgree = termsRepository.findAllById(termsIds);
		for (Terms terms : termsToAgree) {
			if (!userTermsAgreementRepository.existsByUserAndTerms(user, terms)) {
				userTermsAgreementRepository.save(UserTermsAgreement.of(user, terms));
			}
		}
	}
}
