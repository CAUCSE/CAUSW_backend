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

	/**
	 * {@code termsIds}에 담긴 약관 ID 각각에 대해 동의 행이 하나씩 있는지 여부입니다.
	 * 빈 집합이면 {@code true}입니다.
	 */
	public boolean hasAgreedToAllTerms(User user, Set<String> termsIds) {
		if (termsIds == null || termsIds.isEmpty()) {
			return true;
		}
		return userTermsAgreementRepository.countByUserAndTerms_IdIn(user, termsIds) == termsIds.size();
	}
}
