package net.causw.app.main.domain.user.terms.service.implementation;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.terms.entity.Terms;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserTermsAgreementComplianceChecker {

	private final TermsReader termsReader;
	private final UserTermsAgreementReader userTermsAgreementReader;

	/**
	 * 타입별 최신 약관 중 <strong>필수</strong>인 행들에 대해, 유저가 그 약관 ID(최신 필수 버전)에 동의한 기록이 있는지 판별합니다.
	 * 선택 약관은 동의 완료 판정에 포함하지 않습니다.
	 */
	public boolean hasAgreedToAllRequiredLatestTerms(User user) {
		Set<String> requiredLatestTermIds = termsReader.findLatestRequiredVersionPerType().stream()
			.map(Terms::getId)
			.collect(Collectors.toSet());
		return userTermsAgreementReader.hasAgreedToAllTerms(user, requiredLatestTermIds);
	}
}
