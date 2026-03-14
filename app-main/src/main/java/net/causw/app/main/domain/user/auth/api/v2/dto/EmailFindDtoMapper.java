package net.causw.app.main.domain.user.auth.api.v2.dto;

import java.util.List;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.user.auth.api.v2.dto.response.EmailFindResponse;
import net.causw.app.main.domain.user.auth.service.dto.EmailFindResult;

@Mapper(componentModel = "spring")
public interface EmailFindDtoMapper {

	default EmailFindResponse toEmailFindResponse(EmailFindResult emailFindResult) {
		if (emailFindResult == null) {
			return null;
		}
		List<EmailFindResponse.SocialAccountSummary> socialAccounts = emailFindResult.socialAccounts()
			.stream()
			.map(this::toSocialAccountSummary)
			.toList();
		return new EmailFindResponse(emailFindResult.email(), emailFindResult.createdAt(), socialAccounts);
	}

	default EmailFindResponse.SocialAccountSummary toSocialAccountSummary(
		EmailFindResult.SocialAccountSummary socialAccountSummaryResult) {
		if (socialAccountSummaryResult == null) {
			return null;
		}
		return new EmailFindResponse.SocialAccountSummary(
			socialAccountSummaryResult.provider(),
			socialAccountSummaryResult.createdAt());
	}
}
