package net.causw.app.main.domain.user.account.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.query.UserQueryRepository;
import net.causw.app.main.domain.user.account.service.dto.request.UserQueryCondition;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserReader {
	private final UserQueryRepository userQueryRepository;

	public List<User> getUsersByIds(List<String> userIds) {
		return userQueryRepository.findByIds(userIds);
	}

	public List<User> searchByCondition(UserQueryCondition condition) {
		return userQueryRepository.searchByCondition(condition);
	}
}
