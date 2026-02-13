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

	/**
	 * 사용자 상태를 AWAIT으로 변경하고 저장합니다.
	 * (REJECT에서 재학인증 재신청 시 사용)
	 */
	public User updateStateToAwait(User user) {
		user.markAsAwait();
		return this.userRepository.save(user);
	}
}
