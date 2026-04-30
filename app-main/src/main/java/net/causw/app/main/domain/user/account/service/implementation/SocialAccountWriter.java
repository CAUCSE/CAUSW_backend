package net.causw.app.main.domain.user.account.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.SocialAccountRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SocialAccountWriter {

	private final SocialAccountRepository socialAccountRepository;

	@Transactional
	public void deleteSocialAccountsByUsers(List<User> users) {
		List<String> userIds = users.stream()
			.map(User::getId)
			.toList();

		if (userIds.isEmpty()) {
			return;
		}

		List<SocialAccount> socialAccounts = socialAccountRepository.findAllByUserIdIn(userIds);

		if (!socialAccounts.isEmpty()) {
			socialAccountRepository.deleteAll(socialAccounts);
		}
	}

	public void save(SocialAccount socialAccount) {
		socialAccountRepository.save(socialAccount);
	}
}
