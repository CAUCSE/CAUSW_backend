package net.causw.app.main.domain.user.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerLogWriter;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerWriter;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.dto.request.UserListCondition;
import net.causw.app.main.domain.user.account.service.dto.response.UserDetailItem;
import net.causw.app.main.domain.user.account.service.dto.response.UserListItem;
import net.causw.app.main.domain.user.account.service.implementation.UserAdminActionLogWriter;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

	@Mock
	private UserReader userReader;

	@Mock
	private UserWriter userWriter;

	@Mock
	private LockerReader lockerReader;

	@Mock
	private LockerWriter lockerWriter;

	@Mock
	private LockerLogWriter lockerLogWriter;

	@Mock
	private UserAdminActionLogWriter userAdminActionLogWriter;

	@InjectMocks
	private UserAdminService userAdminService;

	/* =========================
	 * 유저 목록 조회
	 * ========================= */
	@Test
	@DisplayName("유저 목록 조회 조건이 주어지면 페이징된 유저 목록을 반환한다")
	void givenUserListCondition_whenGetUserList_thenReturnPagedUserList() {
		// given
		UserListCondition condition = new UserListCondition(
			"홍길동",
			UserState.ACTIVE,
			AcademicStatus.ENROLLED,
			Department.SCHOOL_OF_SW);

		Pageable pageable = PageRequest.of(0, 10);

		User user1 = ObjectFixtures.getCertifiedUserWithId("user-1");
		User user2 = ObjectFixtures.getCertifiedUserWithId("user-2");

		Page<User> users = new PageImpl<>(
			List.of(user1, user2),
			pageable,
			2);

		when(userReader.findUserList(any(UserListCondition.class), any(Pageable.class)))
			.thenReturn(users);

		// when
		Page<UserListItem> result = userAdminService.getUserList(condition, pageable);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(2)
			.extracting(UserListItem::name)
			.containsExactly("name", "name");

		assertThat(result.getTotalElements()).isEqualTo(2);
		assertThat(result.getNumber()).isEqualTo(0);

		verify(userReader).findUserList(condition, pageable);
	}

	/* =========================
	 * 유저 상세 조회
	 * ========================= */
	@Nested
	@DisplayName("유저 상세 조회")
	class GetUserDetail {

		@Test
		@DisplayName("사용자가 존재하면 사용자 상세 정보를 반환한다")
		void givenValidUserId_whenGetUserDetail_thenReturnUserDetail() {
			// given
			String userId = "user-1";
			User user = ObjectFixtures.getCertifiedUserWithId(userId);

			when(userReader.findDetailById(userId)).thenReturn(user);

			// when
			UserDetailItem result = userAdminService.getUserDetail(userId);

			// then
			assertThat(result).isNotNull();
			assertThat(result.id()).isEqualTo(userId);
			assertThat(result.email()).isEqualTo(user.getEmail());
			assertThat(result.name()).isEqualTo(user.getName());

			verify(userReader).findDetailById(userId);
		}

		@Test
		@DisplayName("존재하지 않는 사용자면 USER_NOT_FOUND 예외가 발생한다")
		void givenInvalidUserId_whenGetUserDetail_thenThrowUserNotFound() {
			// given
			String invalidUserId = "invalid-user-id";

			when(userReader.findDetailById(invalidUserId))
				.thenThrow(UserErrorCode.USER_NOT_FOUND.toBaseException());

			// when
			Throwable throwable = catchThrowable(
				() -> userAdminService.getUserDetail(invalidUserId));

			// then
			assertThat(throwable)
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.USER_NOT_FOUND);

			verify(userReader).findDetailById(invalidUserId);
		}
	}

	@Nested
	@DisplayName("유저 추방")
	class DropUser {

		@Test
		@DisplayName("활성 사용자이며 허용된 권한이면 추방하고 사물함을 반납한다")
		void givenDroppableUserWithLocker_whenDropUser_thenDropAndReturnLocker() {
			// given
			User adminUser = ObjectFixtures.getCertifiedUserWithId("admin-1");
			String userId = "user-1";
			String dropReason = "운영 정책 위반";
			User user = ObjectFixtures.getCertifiedUserWithId(userId);
			user.setState(UserState.ACTIVE);
			user.setDeletedAt(null);
			user.setRoles(Set.of(Role.COMMON));
			Locker locker = org.mockito.Mockito.mock(Locker.class);

			when(userReader.findUserById(userId)).thenReturn(user);
			when(lockerReader.findByUserId(userId)).thenReturn(Optional.of(locker));

			// when
			userAdminService.dropUser(adminUser, userId, dropReason);

			// then
			verify(lockerWriter).returnLocker(locker);
			verify(lockerLogWriter).logReturn(locker, user);
			verify(userWriter).dropByAdmin(user, dropReason);
			verify(userAdminActionLogWriter).logDrop(any(), any(), any(), any(), any());
		}

		@Test
		@DisplayName("활성 상태가 아니거나 삭제된 사용자면 USER_NOT_DROPPABLE 예외가 발생한다")
		void givenNotDroppableState_whenDropUser_thenThrowUserNotDroppable() {
			// given
			User adminUser = ObjectFixtures.getCertifiedUserWithId("admin-1");
			String userId = "user-1";
			User user = ObjectFixtures.getCertifiedUserWithId(userId);
			user.setState(UserState.AWAIT);
			user.setDeletedAt(LocalDateTime.now());
			user.setRoles(Set.of(Role.COMMON));

			when(userReader.findUserById(userId)).thenReturn(user);

			// when
			Throwable throwable = catchThrowable(() -> userAdminService.dropUser(adminUser, userId, "reason"));

			// then
			assertThat(throwable)
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.USER_NOT_DROPPABLE);

			verify(lockerReader, never()).findByUserId(any());
			verify(userWriter, never()).dropByAdmin(any(), any());
			verify(userAdminActionLogWriter, never()).logDrop(any(), any(), any(), any(), any());
		}

		@Test
		@DisplayName("추방이 허용되지 않은 권한의 사용자를 추방하면 USER_NOT_DROPPABLE_ROLE 예외가 발생한다")
		void givenNotDroppableRole_whenDropUser_thenThrowUserNotDroppableRole() {
			// given
			User adminUser = ObjectFixtures.getCertifiedUserWithId("admin-1");
			String userId = "user-1";
			User user = ObjectFixtures.getCertifiedUserWithId(userId);
			user.setState(UserState.ACTIVE);
			user.setDeletedAt(null);
			user.setRoles(Set.of(Role.COMMON, Role.ADMIN));

			when(userReader.findUserById(userId)).thenReturn(user);

			// when
			Throwable throwable = catchThrowable(() -> userAdminService.dropUser(adminUser, userId, "reason"));

			// then
			assertThat(throwable)
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.USER_NOT_DROPPABLE_ROLE);

			verify(lockerReader, never()).findByUserId(any());
			verify(userWriter, never()).dropByAdmin(any(), any());
			verify(userAdminActionLogWriter, never()).logDrop(any(), any(), any(), any(), any());
		}
	}

	@Nested
	@DisplayName("유저 복구")
	class RestoreUser {

		@Test
		@DisplayName("DROP 상태 사용자면 복구한다")
		void givenDroppedUser_whenRestoreUser_thenRestore() {
			// given
			User adminUser = ObjectFixtures.getCertifiedUserWithId("admin-1");
			String userId = "user-1";
			User user = ObjectFixtures.getCertifiedUserWithId(userId);
			user.setState(UserState.DROP);
			user.setDeletedAt(null);

			when(userReader.findUserById(userId)).thenReturn(user);

			// when
			userAdminService.restoreUser(adminUser, userId);

			// then
			verify(userWriter).restore(user);
			verify(userAdminActionLogWriter).logRestore(any(), any(), any(), any());
		}

		@Test
		@DisplayName("탈퇴된 사용자면 복구한다")
		void givenDeletedUser_whenRestoreUser_thenRestore() {
			// given
			User adminUser = ObjectFixtures.getCertifiedUserWithId("admin-1");
			String userId = "user-1";
			User user = ObjectFixtures.getCertifiedUserWithId(userId);
			user.setState(UserState.ACTIVE);
			user.setDeletedAt(LocalDateTime.now());

			when(userReader.findUserById(userId)).thenReturn(user);

			// when
			userAdminService.restoreUser(adminUser, userId);

			// then
			verify(userWriter).restore(user);
			verify(userAdminActionLogWriter).logRestore(any(), any(), any(), any());
		}

		@Test
		@DisplayName("DROP도 아니고 탈퇴도 아니면 USER_NOT_RESTORABLE 예외가 발생한다")
		void givenNotRestorableUser_whenRestoreUser_thenThrowUserNotRestorable() {
			// given
			User adminUser = ObjectFixtures.getCertifiedUserWithId("admin-1");
			String userId = "user-1";
			User user = ObjectFixtures.getCertifiedUserWithId(userId);
			user.setState(UserState.ACTIVE);
			user.setDeletedAt(null);

			when(userReader.findUserById(userId)).thenReturn(user);

			// when
			Throwable throwable = catchThrowable(() -> userAdminService.restoreUser(adminUser, userId));

			// then
			assertThat(throwable)
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.USER_NOT_RESTORABLE);
			verify(userWriter, never()).restore(any());
			verify(userAdminActionLogWriter, never()).logRestore(any(), any(), any(), any());
		}
	}

	@Nested
	@DisplayName("유저 권한 변경")
	class UpdateUserRole {

		@Test
		@DisplayName("현재 권한이 일치하면 권한을 변경한다")
		void givenMatchedCurrentRole_whenUpdateUserRole_thenReplaceRole() {
			// given
			User adminUser = ObjectFixtures.getCertifiedUserWithId("admin-1");
			String userId = "user-1";
			User user = ObjectFixtures.getCertifiedUserWithId(userId);
			user.setRoles(Set.of(Role.COMMON));

			when(userReader.findUserById(userId)).thenReturn(user);

			// when
			userAdminService.updateUserRole(adminUser, userId, Role.COMMON, Role.COUNCIL);

			// then
			verify(userWriter).replaceRole(user, Role.COMMON, Role.COUNCIL);
			verify(userAdminActionLogWriter).logRoleChange(any(), any(), any(), any());
		}

		@Test
		@DisplayName("현재 권한이 일치하지 않으면 USER_ROLE_MISMATCH 예외가 발생한다")
		void givenMismatchedCurrentRole_whenUpdateUserRole_thenThrowRoleMismatch() {
			// given
			User adminUser = ObjectFixtures.getCertifiedUserWithId("admin-1");
			String userId = "user-1";
			User user = ObjectFixtures.getCertifiedUserWithId(userId);
			user.setRoles(Set.of(Role.COMMON));

			when(userReader.findUserById(userId)).thenReturn(user);

			// when
			Throwable throwable = catchThrowable(
				() -> userAdminService.updateUserRole(adminUser, userId, Role.COUNCIL, Role.ADMIN));

			// then
			assertThat(throwable)
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.USER_ROLE_MISMATCH);
			verify(userWriter, never()).replaceRole(any(), any(), any());
			verify(userAdminActionLogWriter, never()).logRoleChange(any(), any(), any(), any());
		}
	}
}
