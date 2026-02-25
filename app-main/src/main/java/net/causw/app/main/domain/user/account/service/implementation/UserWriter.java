package net.causw.app.main.domain.user.account.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.SocialAccountRepository;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserWriter {

	private final UserRepository userRepository;
	private final SocialAccountRepository socialAccountRepository;

	public User save(User user) {
		return this.userRepository.save(user);
	}

	public SocialAccount save(SocialAccount socialAccount) {
		return socialAccountRepository.save(socialAccount);
	}
}
