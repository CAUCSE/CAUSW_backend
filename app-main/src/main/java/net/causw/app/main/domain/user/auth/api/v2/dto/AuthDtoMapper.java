package net.causw.app.main.domain.user.auth.api.v2.dto;

import java.util.List;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;
import net.causw.app.main.domain.user.auth.api.v2.dto.response.EmailFindResponse;
import net.causw.app.main.domain.user.auth.api.v2.dto.response.SocialAccountSummaryResponse;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;
import net.causw.app.main.domain.user.auth.service.dto.EmailFindResult;
import net.causw.app.main.domain.user.auth.service.dto.SocialAccountSummaryResult;

@Mapper(componentModel = "spring")
public interface AuthDtoMapper {

	AuthResponse toAuthResponse(AuthResult authResult);

	default EmailFindResponse toFindEmailResponse(EmailFindResult emailFindResult) {
		if (emailFindResult == null) {
			return null;
		}
		List<SocialAccountSummaryResponse> socialAccounts = emailFindResult.socialAccounts() == null
			? List.of()
			: emailFindResult.socialAccounts()
				.stream()
				.map(this::toFindEmailSocialAccountResponse)
				.toList();
		return new EmailFindResponse(emailFindResult.email(), emailFindResult.createdAt(), socialAccounts);
	}

	default SocialAccountSummaryResponse toFindEmailSocialAccountResponse(
		SocialAccountSummaryResult socialAccountSummaryResult) {
		if (socialAccountSummaryResult == null) {
			return null;
		}
		return new SocialAccountSummaryResponse(
			socialAccountSummaryResult.provider(),
			socialAccountSummaryResult.createdAt());
	}
}
