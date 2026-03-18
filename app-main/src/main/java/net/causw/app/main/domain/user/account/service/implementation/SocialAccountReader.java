package net.causw.app.main.domain.user.account.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.repository.user.SocialAccountRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SocialAccountReader {
	private final SocialAccountRepository socialAccountRepository;

	public List<SocialAccount> findAllByUserId(String userId) {
		return socialAccountRepository.findAllByUserId(userId);
	}
}
