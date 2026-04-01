package net.causw.app.main.domain.user.terms.service.implementation;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.terms.entity.Terms;
import net.causw.app.main.domain.user.terms.entity.UserTermsAgreement;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserTermsAgreementComplianceChecker {

	private final TermsReader termsReader;
	private final UserTermsAgreementReader userTermsAgreementReader;

	/**
	 * 타입별 최신 약관 중 <strong>필수</strong>인 것들에 대해 {@link UserTermsAgreement}가 모두 있는지 판별합니다.
	 * 선택 약관은 온보딩/동의 완료 판정에 포함하지 않습니다.
	 */
	public boolean hasAgreedToAllRequiredLatestTerms(User user) {
		Set<String> requiredLatestTermIds = termsReader.findLatestVersionPerType()
			.stream()
			.filter(Terms::isRequired)
			.map(Terms::getId)
			.collect(Collectors.toSet());

		if (requiredLatestTermIds.isEmpty()) {
			return true;
		}

		// 필수 최신 약관 ID에 대한 동의만 조회 (전체 동의 이력을 읽지 않음)
		Set<String> agreedTermIds = userTermsAgreementReader
			.findByUserAndTermsIdIn(user, new ArrayList<>(requiredLatestTermIds))
			.stream()
			.map(UserTermsAgreement::getTerms)
			.map(Terms::getId)
			.collect(Collectors.toSet());

		return agreedTermIds.containsAll(requiredLatestTermIds);
	}
}
