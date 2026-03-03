package net.causw.app.main.domain.user.account.service;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerLogWriter;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.dto.request.UserListCondition;
import net.causw.app.main.domain.user.account.service.dto.response.UserDetailItem;
import net.causw.app.main.domain.user.account.service.dto.response.UserListItem;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAdminService {

	private final UserReader userReader;
	private final UserWriter userWriter;
	private final LockerReader lockerReader;
	private final LockerLogWriter lockerLogWriter;

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

	@Transactional
	public void dropUser(String userId, String dropReason) {
		User targetUser = userReader.findUserById(userId);
		validateDroppableUser(targetUser);

		lockerReader.findByUserId(targetUser.getId()).ifPresent(locker -> {
			locker.returnLocker();
			lockerLogWriter.logReturn(locker, targetUser);
		});

		userWriter.dropByAdmin(targetUser, dropReason);
	}

	@Transactional
	public void restoreUser(String userId) {
		User targetUser = userReader.findUserById(userId);
		validateRestorableUser(targetUser);

		userWriter.restore(targetUser);
	}

	@Transactional
	public void updateUserRole(String userId, Role currentRole, Role newRole) {
		User targetUser = userReader.findUserById(userId);
		validateCurrentRoleMatched(targetUser, currentRole);
		userWriter.replaceRole(targetUser, currentRole, newRole);
	}

	private void validateDroppableUser(User targetUser) {
		boolean isDroppableState = targetUser.getState() == UserState.ACTIVE && !targetUser.isDeleted();
		if (!isDroppableState) {
			throw UserErrorCode.USER_NOT_DROPPABLE.toBaseException();
		}

		Set<Role> targetRoles = targetUser.getRoles();
		boolean isDroppableRole = targetRoles.stream().allMatch(role -> role == Role.COMMON || role == Role.NONE);
		if (!isDroppableRole) {
			throw UserErrorCode.USER_NOT_DROPPABLE_ROLE.toBaseException();
		}
	}

	private void validateRestorableUser(User targetUser) {
		boolean restorable = targetUser.getState() == UserState.DROP || targetUser.isDeleted();
		if (!restorable) {
			throw UserErrorCode.USER_NOT_RESTORABLE.toBaseException();
		}
	}

	private void validateCurrentRoleMatched(User targetUser, Role currentRole) {
		if (!targetUser.getRoles().contains(currentRole)) {
			throw UserErrorCode.USER_ROLE_MISMATCH.toBaseException();
		}
	}
}
