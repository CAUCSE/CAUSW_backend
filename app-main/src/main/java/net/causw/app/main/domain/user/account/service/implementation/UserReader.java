package net.causw.app.main.domain.user.account.service.implementation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.domain.user.account.repository.user.query.UserQueryRepository;
import net.causw.app.main.domain.user.account.service.dto.request.UserListCondition;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserReader {

	private final UserQueryRepository userQueryRepository;
	private final UserRepository userRepository;

	public Page<User> findUserList(
		UserListCondition condition,
		Pageable pageable) {
		return userQueryRepository.findUserList(
			condition.keyword(),
			condition.state(),
			condition.academicStatus(),
			condition.department(),
			pageable);
	}

	public User findById(String userId) {
		return userRepository.findById(userId)
			.orElseThrow(UserErrorCode.USER_NOT_FOUND::toBaseException);
	}

}
