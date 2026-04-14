package net.causw.app.main.domain.user.auth.api.v2.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;
import net.causw.app.main.domain.user.auth.enums.OnboardingStatus;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;

@Mapper(componentModel = "spring")
public interface AuthDtoMapper {

	@Mapping(target = "onboardingStatus", expression = "java(resolveOnboardingStatus(authResult.isGuest(), authResult.isTermsAgreed(), authResult.isAcademicCertified()))")
	AuthResponse toAuthResponse(AuthResult authResult);

	default OnboardingStatus resolveOnboardingStatus(boolean isGuest, boolean isTermsAgreed,
		boolean isAcademicCertified) {
		return OnboardingStatus.resolve(isGuest, isTermsAgreed, isAcademicCertified);
	}
}
