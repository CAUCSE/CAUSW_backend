package net.causw.app.main.domain.user.terms.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.terms.entity.UserTermsAgreement;
import net.causw.app.main.domain.user.terms.repository.UserTermsAgreementRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserTermsAgreementWriter {

	private final UserTermsAgreementRepository userTermsAgreementRepository;

	public void saveAll(List<UserTermsAgreement> agreements) {
		userTermsAgreementRepository.saveAll(agreements);
	}
}
