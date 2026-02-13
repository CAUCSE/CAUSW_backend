package net.causw.app.main.domain.user.account.service.v2.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserWriter {

	private final UserRepository userRepository;

	public User save(User user) {
		return this.userRepository.save(user);
	}
}
