package net.causw.app.main.domain.user.auth.api.v2.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;
import net.causw.app.main.domain.user.auth.enums.OnboardingStatus;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;
import net.causw.app.main.domain.user.auth.service.dto.SignInResult;

@Mapper(componentModel = "spring")
public interface AuthDtoMapper {

	@Mapping(target = "onboardingStatus", expression = "java(resolveOnboardingStatus(authResult.isGuest(), authResult.hasAllRequiredLatestTerms(), authResult.isAcademicCertified()))")
	AuthResponse toAuthResponse(AuthResult authResult);

	@Mapping(target = "onboardingStatus", expression = "java(resolveOnboardingStatus(signInResult.isGuest(), signInResult.isTermsAgreed(), signInResult.isAcademicCertified()))")
	AuthResponse toAuthResponse(SignInResult signInResult);

	default OnboardingStatus resolveOnboardingStatus(boolean isGuest, boolean hasAllRequiredLatestTerms,
		boolean isAcademicCertified) {
		return OnboardingStatus.resolve(isGuest, hasAllRequiredLatestTerms, isAcademicCertified);
	}
}
