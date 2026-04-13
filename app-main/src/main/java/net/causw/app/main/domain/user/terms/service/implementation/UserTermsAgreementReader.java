package net.causw.app.main.domain.user.terms.service.implementation;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.terms.entity.UserTermsAgreement;
import net.causw.app.main.domain.user.terms.repository.UserTermsAgreementRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserTermsAgreementReader {

	private final UserTermsAgreementRepository userTermsAgreementRepository;

	public List<UserTermsAgreement> findByUser(User user) {
		return userTermsAgreementRepository.findByUser(user);
	}

	public List<UserTermsAgreement> findByUserAndTermsIdIn(User user, List<String> termsIds) {
		return userTermsAgreementRepository.findByUserAndTerms_IdIn(user, termsIds);
	}

	public boolean hasAgreedToAllRequiredLatestTerms(User user) {
		return userTermsAgreementRepository.hasAgreedToAllRequiredLatestTerms(user);
	}
}
