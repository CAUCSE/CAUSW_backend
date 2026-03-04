package net.causw.app.main.domain.user.account.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerLogWriter;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.dto.request.UserListCondition;
import net.causw.app.main.domain.user.account.service.dto.response.UserDetailItem;
import net.causw.app.main.domain.user.account.service.dto.response.UserListItem;
import net.causw.app.main.domain.user.account.service.implementation.UserAdminActionLogWriter;
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
	private final LockerWriter lockerWriter;
	private final LockerLogWriter lockerLogWriter;
	private final UserAdminActionLogWriter userAdminActionLogWriter;

	// 필터링 조건과 페이징 정보를 기반으로 전체 사용자 목록 조회
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
	public void dropUser(User adminUser, String userId, String dropReason) {
		User targetUser = userReader.findUserById(userId);
		validateDroppableUser(targetUser);

		UserState beforeState = targetUser.getState();
		Set<Role> beforeRoles = new HashSet<>(targetUser.getRoles());

		lockerReader.findByUserId(targetUser.getId()).ifPresent(locker -> {
			lockerWriter.returnLocker(locker);
			lockerLogWriter.logReturn(locker, targetUser);
		});

		userWriter.dropByAdmin(targetUser, dropReason);
		userAdminActionLogWriter.logDrop(adminUser, targetUser, beforeState, beforeRoles, dropReason);
	}

	@Transactional
	public void restoreUser(User adminUser, String userId) {
		User targetUser = userReader.findUserById(userId);
		validateRestorableUser(targetUser);

		UserState beforeState = targetUser.getState();
		Set<Role> beforeRoles = new HashSet<>(targetUser.getRoles());

		userWriter.restore(targetUser);
		userAdminActionLogWriter.logRestore(adminUser, targetUser, beforeState, beforeRoles);
	}

	@Transactional
	public void replaceUserRole(User adminUser, String userId, Role currentRole, Role newRole) {
		User targetUser = userReader.findUserById(userId);
		validateCurrentRoleMatched(targetUser, currentRole);

		Set<Role> beforeRoles = new HashSet<>(targetUser.getRoles());

		userWriter.replaceRole(targetUser, currentRole, newRole);
		userAdminActionLogWriter.logRoleChange(adminUser, targetUser, beforeRoles, targetUser.getRoles());
	}

	// 대상 사용자가 추방 가능한 상태인지 확인
	// - ACTIVE 상태여야 하고 이미 탈퇴한(isDeleted) 사용자가 아님
	// - 권한 있는 역할(ADMIN 등)을 가지고 있으면 추방 불가
	private void validateDroppableUser(User targetUser) {
		boolean isDroppableState = targetUser.getState() == UserState.ACTIVE && !targetUser.isDeleted();
		if (!isDroppableState) {
			throw UserErrorCode.USER_NOT_DROPPABLE.toBaseException();
		}

		Set<Role> targetRoles = targetUser.getRoles();
		boolean isDroppableRole = targetRoles.stream()
			.noneMatch(Role.getPrivilegedRoles()::contains);
		if (!isDroppableRole) {
			throw UserErrorCode.USER_NOT_DROPPABLE_ROLE.toBaseException();
		}
	}

	// 사용자 복원이 가능한지 검증
	// - DROP 상태만 복원 가능
	private void validateRestorableUser(User targetUser) {
		boolean restorable = targetUser.getState() == UserState.DROP;
		if (!restorable) {
			throw UserErrorCode.USER_NOT_RESTORABLE.toBaseException();
		}
	}

	// 요청된 currentRole이 사용자의 실제 역할과 일치하는지 검증
	private void validateCurrentRoleMatched(User targetUser, Role currentRole) {
		if (!targetUser.getRoles().contains(currentRole)) {
			throw UserErrorCode.USER_ROLE_MISMATCH.toBaseException();
		}
	}
}
