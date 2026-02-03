package net.causw.app.main.domain.user.account.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.service.dto.request.UserQueryCondition;
import net.causw.app.main.domain.user.account.service.dto.result.UserSearchListResult;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

	private final UserReader userReader;

	public UserSearchListResult searchUsers(UserQueryCondition condition) {
		return UserSearchListResult.from(userReader.searchByCondition(condition));
	}
}
