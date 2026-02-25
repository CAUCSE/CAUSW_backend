package net.causw.app.main.domain.user.account.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.dto.request.UserListCondition;
import net.causw.app.main.domain.user.account.service.dto.response.UserDetailItem;
import net.causw.app.main.domain.user.account.service.dto.response.UserListItem;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAdminService {

	private final UserReader userReader;

	@Transactional(readOnly = true)
	public Page<UserListItem> getUserList(
		UserListCondition condition,
		Pageable pageable) {
		return userReader.findUserList(condition, pageable)
			.map(UserListItem::from);
	}

	@Transactional(readOnly = true)
	public UserDetailItem getUserDetail(String userId) {
		User user = userReader.findDetailById(userId);
		return UserDetailItem.from(user);
	}
}
