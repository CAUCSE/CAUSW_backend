package net.causw.app.main.domain.user.account.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.asset.file.service.v2.implementation.UserProfileImageReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.dto.request.DeletedUserQueryCondition;
import net.causw.app.main.domain.user.account.service.dto.request.UserListCondition;
import net.causw.app.main.domain.user.account.service.dto.response.UserDetailItem;
import net.causw.app.main.domain.user.account.service.dto.response.UserDropResult;
import net.causw.app.main.domain.user.account.service.dto.response.UserListItem;
import net.causw.app.main.domain.user.account.service.dto.response.UserRestoreResult;
import net.causw.app.main.domain.user.account.service.dto.response.UserRestoreWithdrawalResult;
import net.causw.app.main.domain.user.account.service.dto.response.UserRoleUpdateResult;
import net.causw.app.main.domain.user.account.service.dto.result.DeletedUserListItemDto;
import net.causw.app.main.domain.user.account.service.implementation.DroppedUserIdentifierWriter;
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
	private final UserAdminActionLogWriter userAdminActionLogWriter;
	private final UserProfileImageReader userProfileImageReader;
	private final DroppedUserIdentifierWriter droppedUserIdentifierWriter;
	private final UserAccountService userAccountService;

	// 필터링 조건과 페이징 정보를 기반으로 전체 사용자 목록 조회
	@Transactional(readOnly = true)
	public Page<UserListItem> getUserList(UserListCondition condition, Pageable pageable) {
		return userReader.findUserList(condition, pageable);
	}

	@Transactional(readOnly = true)
	public Page<DeletedUserListItemDto> getDeletedUserList(
		DeletedUserQueryCondition condition,
		Pageable pageable) {
		return userReader.findDeletedUserList(condition, pageable);
	}

	@Transactional(readOnly = true)
	public UserDetailItem getUserDetail(String userId) {
		// todo: major deprecated 제거
		User user = userReader.findDetailById(userId);
		UserProfileImage profileImage = userProfileImageReader.findByUserIdOrNull(userId);
		return UserDetailItem.from(user, profileImage);
	}

	@Transactional
	public UserDropResult dropUser(User adminUser, String userId, String dropReason) {
		User targetUser = userReader.findUserById(userId);
		validateDroppableUser(targetUser);

		UserState beforeState = targetUser.getState();
		Set<Role> beforeRoles = new HashSet<>(targetUser.getRoles());

		lockerReader.findByUserId(targetUser.getId()).ifPresent(locker -> {
			lockerWriter.releaseLocker(locker, adminUser, targetUser.getEmail(), targetUser.getName());
		});

		User updatedUser = userWriter.dropByAdmin(targetUser, dropReason);

		droppedUserIdentifierWriter.saveDroppedIdentifiers(updatedUser);

		userAdminActionLogWriter.logDrop(adminUser, updatedUser, beforeState, beforeRoles, dropReason);
		return UserDropResult.from(updatedUser);
	}

	@Transactional
	public UserRestoreResult restoreUser(User adminUser, String userId) {
		User targetUser = userReader.findUserById(userId);

		validateRestorableUser(targetUser);

		UserState beforeState = targetUser.getState();
		Set<Role> beforeRoles = new HashSet<>(targetUser.getRoles());

		User restoredUser = userAccountService.restore(targetUser.getId());
		userAdminActionLogWriter.logRestore(adminUser, restoredUser, beforeState, beforeRoles);
		return UserRestoreResult.from(restoredUser);
	}

	@Transactional
	public UserRestoreWithdrawalResult restoreWithdrawnUser(User adminUser, String userId) {
		User targetUser = userReader.findUserById(userId);

		validateRestorableWithdrawnUser(targetUser);

		UserState beforeState = targetUser.getState();
		Set<Role> beforeRoles = new HashSet<>(targetUser.getRoles());

		User restoredUser = userAccountService.restore(targetUser.getId());

		userAdminActionLogWriter.logRestore(adminUser, restoredUser, beforeState, beforeRoles);

		return UserRestoreWithdrawalResult.from(restoredUser);
	}

	@Transactional
	public UserRoleUpdateResult replaceUserRole(User adminUser, String userId, Role currentRole, Role newRole) {
		User targetUser = userReader.findUserById(userId);
		validateRoleUpdatableUser(targetUser);
		validateCurrentRoleMatched(targetUser, currentRole);

		Set<Role> beforeRoles = new HashSet<>(targetUser.getRoles());

		User updatedUser = userWriter.replaceRole(targetUser, currentRole, newRole);
		userAdminActionLogWriter.logRoleChange(adminUser, updatedUser, beforeRoles, updatedUser.getRoles());
		return UserRoleUpdateResult.from(updatedUser);
	}

	// 대상 사용자가 추방 가능한 상태인지 확인
	private void validateDroppableUser(User targetUser) {
		if (!targetUser.isDroppable()) {
			throw UserErrorCode.USER_NOT_DROPPABLE.toBaseException();
		}
	}

	// 사용자 복원이 가능한지 검증
	// - DROP 상태만 복원 가능
	private void validateRestorableUser(User targetUser) {
		if (targetUser.getState() != UserState.DROP) {
			throw UserErrorCode.USER_NOT_RESTORABLE.toBaseException();
		}
	}

	// 자진 탈퇴 사용자 복원이 가능한지 검증
	// - DROP 상태가 아니어야 하며, 실제로 탈퇴(isDeleted) 상태여야 함
	private void validateRestorableWithdrawnUser(User targetUser) {
		// 1. 추방(DROP)된 유저는 이 API를 통해 복구할 수 없음 (restoreUser API 사용 유도)
		if (targetUser.getState() == UserState.DROP) {
			throw UserErrorCode.USER_NOT_RESTORABLE.toBaseException();
		}

		// 2. 실제로 탈퇴(isDeleted) 상태인 유저여야 함
		if (!targetUser.isDeleted()) {
			throw UserErrorCode.USER_NOT_RESTORABLE.toBaseException();
		}
	}

	// 요청된 currentRole이 사용자의 실제 역할과 일치하는지 검증
	private void validateCurrentRoleMatched(User targetUser, Role currentRole) {
		if (!targetUser.getRoles().contains(currentRole)) {
			throw UserErrorCode.USER_ROLE_MISMATCH.toBaseException();
		}
	}

	// 역할 변경 가능한 대상인지 검증
	// - ACTIVE 상태여야 함
	private void validateRoleUpdatableUser(User targetUser) {
		boolean roleUpdatable = targetUser.getState() == UserState.ACTIVE;
		if (!roleUpdatable) {
			throw UserErrorCode.USER_NOT_ROLE_UPDATABLE.toBaseException();
		}
	}
}
