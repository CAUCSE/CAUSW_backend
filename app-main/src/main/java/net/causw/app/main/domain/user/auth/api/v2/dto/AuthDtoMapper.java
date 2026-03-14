package net.causw.app.main.domain.user.auth.api.v2.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;
import net.causw.app.main.domain.user.auth.api.v2.dto.response.OnboardingStatus;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;

@Mapper(componentModel = "spring")
public interface AuthDtoMapper {

	@Mapping(target = "onboardingStatus", expression = "java(resolveOnboardingStatus(authResult.isTermsAgreed(), authResult.isAcademicCertified()))")
	AuthResponse toAuthResponse(AuthResult authResult);

	default OnboardingStatus resolveOnboardingStatus(boolean isTermsAgreed, boolean isAcademicCertified) {
		if (!isTermsAgreed) {
			return OnboardingStatus.TERMS_REQUIRED;
		}
		if (!isAcademicCertified) {
			return OnboardingStatus.ACADEMIC_CERTIFICATION_REQUIRED;
		}
		return OnboardingStatus.ACTIVE;
	}
}
