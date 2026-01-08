package net.causw.app.main.domain.user.account.service;

import java.util.List;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.domain.user.account.repository.user.query.UserQueryRepository;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserEntityService {

	private final UserQueryRepository userQueryRepository;
	private final UserRepository userRepository;

	public List<User> findAllActiveUsersByRoles(List<Role> roles) {
		return userQueryRepository.findAllActiveUsersByRoles(roles);
	}

	/**
	 * @param userId 유저 아이디
	 * @return 유저
	 */
	public User findUserByUserId(String userId) {

		return userRepository.findById(userId)
			.orElseThrow(() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.USER_NOT_FOUND));
	}
}
