package net.causw.app.main.domain.asset.locker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

import net.causw.app.main.domain.admin.audit.event.AdminAuditLogEventPublisher;
import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerLocation;
import net.causw.app.main.domain.asset.locker.entity.LockerLog;
import net.causw.app.main.domain.asset.locker.entity.LockerName;
import net.causw.app.main.domain.asset.locker.service.dto.LockerListCondition;
import net.causw.app.main.domain.asset.locker.service.dto.LockerLogListCondition;
import net.causw.app.main.domain.asset.locker.service.implementation.LockerLogReader;
import net.causw.app.main.domain.asset.locker.service.implementation.LockerReader;
import net.causw.app.main.domain.asset.locker.service.implementation.LockerValidator;
import net.causw.app.main.domain.asset.locker.service.implementation.LockerWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.LockerErrorCode;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
@DisplayName("LockerAdminService 단위 테스트")
class LockerAdminServiceTest {

	@InjectMocks
	private LockerAdminService lockerAdminService;

	@Mock
	private LockerReader lockerReader;
	@Mock
	private LockerLogReader lockerLogReader;
	@Mock
	private LockerValidator lockerValidator;
	@Mock
	private LockerWriter lockerWriter;
	@Mock
	private LockerExpirationService lockerExpirationService;
	@Mock
	private AdminAuditLogEventPublisher adminAuditLogEventPublisher;
	@Mock
	private UserReader userReader;

	private User createUser(String userId) {
		return ObjectFixtures.getCertifiedUserWithId(userId);
	}

	private LockerLocation createLocation(String id, LockerName name) {
		return ObjectFixtures.getLockerLocationWithId(name, id);
	}

	private Locker createLocker(String id, long number, LockerLocation location, User user, LocalDateTime expiredAt,
		boolean isActive) {
		return spy(ObjectFixtures.getLockerWithId(id, number, isActive, user, location, expiredAt));
	}

	@Nested
	@DisplayName("getLockerLogList")
	class GetLockerLogList {

		@Test
		@DisplayName("성공: 로그 리더에 조건을 전달해 페이지를 조회한다")
		void givenSearchCondition_whenGetLockerLogList_thenDelegatesToReader() {
			// given
			LockerLogListCondition condition = new LockerLogListCondition(
				"keyword", null, null, null);
			PageRequest pageRequest = PageRequest.of(0, 10);

			Page<LockerLog> expectedPage = new PageImpl<>(List.of());
			when(lockerLogReader.findLockerLogList(
				anyString(), any(), any(), any(), any())).thenReturn(expectedPage);

			// when
			Page<LockerLog> result = lockerAdminService.getLockerLogList(condition, pageRequest);

			// then
			assertThat(result).isSameAs(expectedPage);
			verify(lockerLogReader).findLockerLogList(
				condition.userKeyword(),
				condition.action(),
				condition.lockerLocationName(),
				condition.lockerNumber(),
				pageRequest);
		}
	}

	@Nested
	@DisplayName("getLockerList")
	class GetLockerList {

		@Test
		@DisplayName("성공: 사물함 리더에 조건을 전달해 페이지를 조회한다")
		void givenSearchCondition_whenGetLockerList_thenDelegatesToReader() {
			// given
			LockerListCondition condition = new LockerListCondition(
				"keyword", null, true, false, null);
			PageRequest pageRequest = PageRequest.of(0, 10);

			Page<Locker> expectedPage = new PageImpl<>(List.of());
			when(lockerReader.findLockerList(
				anyString(), any(), any(), any(), any(), any())).thenReturn(expectedPage);

			// when
			Page<Locker> result = lockerAdminService.getLockerList(condition, pageRequest);

			// then
			assertThat(result).isSameAs(expectedPage);
			verify(lockerReader).findLockerList(
				condition.userKeyword(),
				condition.location(),
				condition.isActive(),
				condition.isOccupied(),
				condition.isExpired(),
				pageRequest);
		}
	}

	@Nested
	@DisplayName("assignLocker")
	class AssignLocker {

		@Test
		@DisplayName("성공: 검증 후 사물함을 사용자에게 배정하고 로그를 남긴다")
		void givenAssignableLockerAndUser_whenAssignLocker_thenRegistersAndLogsAdminAssign() {
			// given
			String lockerId = "locker-1";
			String userId = "user-1";
			String adminId = "admin-1";
			LocalDateTime expiredAt = LocalDateTime.now().plusDays(30);

			User admin = createUser(adminId);
			User user = createUser(userId);
			LockerLocation location = createLocation("loc-1", LockerName.SECOND);
			Locker locker = createLocker("locker-1", 1L, location, null, null, true);

			when(lockerReader.findByIdForWrite(lockerId)).thenReturn(locker);
			when(userReader.findAdminUserById(adminId)).thenReturn(admin);
			when(userReader.findUserByIdNotDeleted(userId)).thenReturn(user);

			// when
			lockerAdminService.assignLocker(lockerId, userId, expiredAt, adminId);

			// then
			verify(lockerValidator).validateAssignable(locker);
			verify(lockerValidator).validateUserNotHavingLocker(userId);

			verify(lockerWriter).assignLocker(locker, admin, user, expiredAt);
			verify(adminAuditLogEventPublisher).publishLockerAssign(locker, admin, user, expiredAt);
		}

		@Test
		@DisplayName("실패: 유저가 이미 사물함을 보유한 경우 예외를 그대로 전달한다")
		void givenUserAlreadyHasLocker_whenAssignLocker_thenThrowsLockerUserAlreadyHasLocker() {
			// given
			String lockerId = "locker-1";
			String userId = "user-1";
			String adminId = "admin-1";

			User admin = createUser(adminId);
			LockerLocation location = createLocation("loc-1", LockerName.SECOND);
			Locker locker = createLocker("locker-1", 1L, location, null, null, true);

			when(lockerReader.findByIdForWrite(lockerId)).thenReturn(locker);
			when(userReader.findAdminUserById(adminId)).thenReturn(admin);

			BaseRunTimeV2Exception exception = LockerErrorCode.LOCKER_USER_ALREADY_HAS_LOCKER.toBaseException();
			doThrow(exception)
				.when(lockerValidator)
				.validateUserNotHavingLocker(userId);

			// when & then
			assertThatThrownBy(() -> lockerAdminService.assignLocker(lockerId, userId, LocalDateTime.now(), adminId))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_USER_ALREADY_HAS_LOCKER.getMessage());

			verify(lockerWriter, never()).assignLocker(any(Locker.class), any(User.class), any(User.class),
				any(LocalDateTime.class));
			verifyNoInteractions(adminAuditLogEventPublisher);
		}
	}

	@Nested
	@DisplayName("extendLocker")
	class ExtendLocker {

		@Test
		@DisplayName("성공: 사용 중인 사물함의 만료일을 연장하고 로그를 남긴다")
		void givenInUseLocker_whenExtendLockerByAdmin_thenExtendsExpireDateAndLogs() {
			// given
			String lockerId = "locker-1";
			String adminId = "admin-1";
			LocalDateTime newExpireAt = LocalDateTime.now().plusDays(30);

			User admin = createUser(adminId);
			User lockerUser = createUser("user-1");
			LockerLocation location = createLocation("loc-1", LockerName.SECOND);
			Locker locker = createLocker("locker-1", 1L, location, lockerUser, null, true);

			when(lockerReader.findByIdForWrite(lockerId)).thenReturn(locker);
			when(userReader.findAdminUserById(adminId)).thenReturn(admin);

			// when
			lockerAdminService.extendLocker(lockerId, newExpireAt, adminId);

			// then
			verify(lockerValidator).validateInUse(locker);
			verify(lockerWriter).extendLockerByAdmin(locker, admin, lockerUser, newExpireAt);
			verify(adminAuditLogEventPublisher).publishLockerExtend(locker, admin, lockerUser, newExpireAt);
		}

		@Test
		@DisplayName("실패: 사용 중인 사물함이 아니면 예외를 그대로 전달한다")
		void givenNotInUseLocker_whenExtendLockerByAdmin_thenThrowsLockerNotInUse() {
			// given
			String lockerId = "locker-1";
			String adminId = "admin-1";

			User admin = createUser(adminId);
			LockerLocation location = createLocation("loc-1", LockerName.SECOND);
			Locker locker = createLocker("locker-1", 1L, location, null, null, true);

			when(lockerReader.findByIdForWrite(lockerId)).thenReturn(locker);
			when(userReader.findAdminUserById(adminId)).thenReturn(admin);

			BaseRunTimeV2Exception exception = LockerErrorCode.LOCKER_NOT_IN_USE.toBaseException();
			doThrow(exception)
				.when(lockerValidator)
				.validateInUse(locker);

			// when & then
			assertThatThrownBy(() -> lockerAdminService.extendLocker(lockerId, LocalDateTime.now(), adminId))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_NOT_IN_USE.getMessage());

			verify(lockerWriter, never()).extendLockerByAdmin(any(Locker.class), any(User.class), any(User.class),
				any(LocalDateTime.class));
			verifyNoInteractions(adminAuditLogEventPublisher);
		}
	}

	@Nested
	@DisplayName("releaseLocker")
	class ReleaseLocker {

		@Test
		@DisplayName("성공: 사용 중인 사물함을 회수하고 로그를 남긴다")
		void givenInUseLocker_whenReleaseLockerByAdmin_thenReturnsLockerAndLogs() {
			// given
			String lockerId = "locker-1";
			String adminId = "admin-1";

			Locker locker = mock(Locker.class);
			User admin = mock(User.class);
			User lockerUser = mock(User.class);
			when(lockerUser.getEmail()).thenReturn("user@cau.ac.kr");
			when(lockerUser.getName()).thenReturn("lockerUser");

			when(lockerReader.findByIdForWrite(lockerId)).thenReturn(locker);
			when(userReader.findAdminUserById(adminId)).thenReturn(admin);
			when(locker.getUser()).thenReturn(Optional.of(lockerUser));

			// when
			lockerAdminService.releaseLocker(lockerId, adminId);

			// then
			verify(lockerValidator).validateInUse(locker);
			verify(lockerWriter).releaseLocker(locker, admin, lockerUser.getEmail(), lockerUser.getName());
			verify(adminAuditLogEventPublisher).publishLockerRelease(locker, admin, lockerUser);
		}

		@Test
		@DisplayName("실패: 사용 중인 사물함이 아니면 예외를 그대로 전달한다")
		void givenNotInUseLocker_whenReleaseLockerByAdmin_thenThrowsLockerNotInUse() {
			// given
			String lockerId = "locker-1";
			String adminId = "admin-1";

			Locker locker = mock(Locker.class);
			User admin = mock(User.class);

			when(lockerReader.findByIdForWrite(lockerId)).thenReturn(locker);
			when(userReader.findAdminUserById(adminId)).thenReturn(admin);

			BaseRunTimeV2Exception exception = LockerErrorCode.LOCKER_NOT_IN_USE.toBaseException();
			doThrow(exception)
				.when(lockerValidator)
				.validateInUse(locker);

			// when & then
			assertThatThrownBy(() -> lockerAdminService.releaseLocker(lockerId, adminId))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_NOT_IN_USE.getMessage());

			verify(lockerWriter, never()).releaseLocker(any(Locker.class), any(User.class), anyString(), anyString());
			verifyNoInteractions(adminAuditLogEventPublisher);
		}
	}

	@Nested
	@DisplayName("enableLocker")
	class EnableLocker {

		@Test
		@DisplayName("성공: 비활성 사물함을 활성화하고 로그를 남긴다")
		void givenDisableableLocker_whenEnableLocker_thenEnablesAndLogs() {
			// given
			String lockerId = "locker-1";
			String adminId = "admin-1";

			Locker locker = mock(Locker.class);
			User admin = mock(User.class);

			when(lockerReader.findByIdForWrite(lockerId)).thenReturn(locker);
			when(userReader.findAdminUserById(adminId)).thenReturn(admin);

			// when
			lockerAdminService.enableLocker(lockerId, adminId);

			// then
			verify(lockerValidator).validateEnableable(locker);
			verify(lockerWriter).enableLocker(locker, admin);
			verify(adminAuditLogEventPublisher).publishLockerEnable(locker, admin);
		}
	}

	@Nested
	@DisplayName("disableLocker")
	class DisableLocker {

		@Test
		@DisplayName("성공: 사물함을 비활성화하고, 사용 중이면 회수 후 로그를 남긴다")
		void givenInUseLocker_whenDisableLocker_thenReturnsLockerDisablesAndLogs() {
			// given
			String lockerId = "locker-1";
			String adminId = "admin-1";

			User admin = createUser(adminId);
			User currentUser = createUser("user-current");
			LockerLocation location = createLocation("loc-1", LockerName.SECOND);
			Locker locker = createLocker("locker-1", 1L, location, currentUser, LocalDateTime.now().plusDays(1), true);

			when(lockerReader.findByIdForWrite(lockerId)).thenReturn(locker);
			when(userReader.findAdminUserById(adminId)).thenReturn(admin);
			when(locker.getUser()).thenReturn(Optional.of(currentUser));

			// when
			lockerAdminService.disableLocker(lockerId, adminId);

			// then
			verify(lockerValidator).validateDisableable(locker);

			verify(lockerWriter).releaseLocker(locker, admin, currentUser.getEmail(), currentUser.getName());
			verify(lockerWriter).disableLocker(locker, admin);
			verify(adminAuditLogEventPublisher).publishLockerDisable(locker, admin, Optional.of(currentUser));
		}

		@Test
		@DisplayName("성공: 사용 중이 아닌 사물함은 단순 비활성화만 수행한다")
		void givenNotInUseLocker_whenDisableLocker_thenDisablesAndLogsWithoutReturn() {
			// given
			String lockerId = "locker-1";
			String adminId = "admin-1";

			User admin = createUser(adminId);
			LockerLocation location = createLocation("loc-1", LockerName.SECOND);
			Locker locker = createLocker("locker-1", 1L, location, null, null, true);
			when(lockerReader.findByIdForWrite(lockerId)).thenReturn(locker);
			when(userReader.findAdminUserById(adminId)).thenReturn(admin);
			when(locker.getUser()).thenReturn(Optional.empty());

			// when
			lockerAdminService.disableLocker(lockerId, adminId);

			// then
			verify(lockerValidator).validateDisableable(locker);

			verify(lockerWriter, never()).releaseLocker(any(Locker.class), any(User.class), anyString(), anyString());
			verify(lockerWriter).disableLocker(locker, admin);
			verify(adminAuditLogEventPublisher).publishLockerDisable(locker, admin, Optional.empty());
		}
	}

	@Nested
	@DisplayName("releaseExpiredLocker")
	class ReleaseExpiredLocker {

		@Test
		@DisplayName("성공: 관리자를 확인하고 만료 사물함 일괄 반납을 위임한다")
		void givenAdmin_whenReleaseExpiredLocker_thenDelegatesToExpirationService() {
			// given
			String adminId = "admin-1";
			User admin = createUser(adminId);

			when(userReader.findAdminUserById(adminId)).thenReturn(admin);

			// when
			lockerAdminService.releaseExpiredLocker(adminId);

			// then
			verify(userReader).findAdminUserById(adminId);
			verify(lockerExpirationService).releaseExpiredLockers(admin);
		}
	}
}
