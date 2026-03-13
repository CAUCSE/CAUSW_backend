package net.causw.app.main.domain.user.auth.api.v2.dto;

import java.util.List;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;
import net.causw.app.main.domain.user.auth.api.v2.dto.response.FindEmailResponse;
import net.causw.app.main.domain.user.auth.api.v2.dto.response.FindEmailSocialAccountResponse;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;
import net.causw.app.main.domain.user.auth.service.dto.FindEmailResult;
import net.causw.app.main.domain.user.auth.service.dto.FindEmailSocialAccountResult;

@Mapper(componentModel = "spring")
public interface AuthDtoMapper {

	AuthResponse toAuthResponse(AuthResult authResult);

	default FindEmailResponse toFindEmailResponse(FindEmailResult findEmailResult) {
		if (findEmailResult == null) {
			return null;
		}
		List<FindEmailSocialAccountResponse> socialAccounts = findEmailResult.socialAccounts() == null
			? List.of()
			: findEmailResult.socialAccounts()
				.stream()
				.map(this::toFindEmailSocialAccountResponse)
				.toList();
		return new FindEmailResponse(findEmailResult.email(), findEmailResult.createdAt(), socialAccounts);
	}

	default FindEmailSocialAccountResponse toFindEmailSocialAccountResponse(
		FindEmailSocialAccountResult findEmailSocialAccountResult) {
		if (findEmailSocialAccountResult == null) {
			return null;
		}
		return new FindEmailSocialAccountResponse(
			findEmailSocialAccountResult.provider(),
			findEmailSocialAccountResult.createdAt());
	}
}
