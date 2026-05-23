package net.causw.app.main.domain.user.account.service.implementation;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.enums.user.SocialType;
import net.causw.app.main.domain.user.account.repository.user.SocialAccountRepository;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SocialAccountReader {
	private final SocialAccountRepository socialAccountRepository;

	public List<SocialAccount> findAllByUserId(String userId) {
		return socialAccountRepository.findAllByUserId(userId);
	}

	public Optional<SocialAccount> findByUserIdAndSocialType(String userId, SocialType socialType) {
		return socialAccountRepository.findByUser_IdAndSocialType(userId, socialType);
	}

	public SocialAccount findByUserIdAndSocialTypeOrElseThrow(String userId, SocialType socialType) {
		return socialAccountRepository.findByUser_IdAndSocialType(userId, socialType)
			.orElseThrow(AuthErrorCode.SOCIAL_ACCOUNT_NOT_FOUND::toBaseException);
	}

	public long countByUserId(String userId) {
		return socialAccountRepository.countByUserId(userId);
	}

	public List<SocialAccount> findAllByUserIdIn(List<String> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			return List.of();
		}
		return socialAccountRepository.findAllByUserIdIn(userIds);
	}
}
