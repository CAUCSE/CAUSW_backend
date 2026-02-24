package net.causw.app.main.domain.asset.locker.service.v2;

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

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerLocation;
import net.causw.app.main.domain.asset.locker.entity.LockerLog;
import net.causw.app.main.domain.asset.locker.entity.LockerName;
import net.causw.app.main.domain.asset.locker.service.v2.dto.LockerListCondition;
import net.causw.app.main.domain.asset.locker.service.v2.dto.LockerLogListCondition;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerLogReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerLogWriter;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerValidator;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;
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
	private LockerLogWriter lockerLogWriter;
	@Mock
	private LockerValidator lockerValidator;
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

			verify(locker).register(user, expiredAt);
			verify(lockerLogWriter).logAdminAssign(locker, admin);
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

			verify(locker, never()).register(any(User.class), any(LocalDateTime.class));
			verify(lockerLogWriter, never()).logAdminAssign(any(Locker.class), any(User.class));
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
			LockerLocation location = createLocation("loc-1", LockerName.SECOND);
			Locker locker = createLocker("locker-1", 1L, location, null, null, true);

			when(lockerReader.findByIdForWrite(lockerId)).thenReturn(locker);
			when(userReader.findAdminUserById(adminId)).thenReturn(admin);

			// when
			lockerAdminService.extendLocker(lockerId, newExpireAt, adminId);

			// then
			verify(lockerValidator).validateInUse(locker);
			verify(locker).extendExpireDate(newExpireAt);
			verify(lockerLogWriter).logAdminExtend(locker, admin);
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

			verify(locker, never()).extendExpireDate(any(LocalDateTime.class));
			verify(lockerLogWriter, never()).logAdminExtend(any(Locker.class), any(User.class));
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

			when(lockerReader.findByIdForWrite(lockerId)).thenReturn(locker);
			when(userReader.findAdminUserById(adminId)).thenReturn(admin);

			// when
			lockerAdminService.releaseLocker(lockerId, adminId);

			// then
			verify(lockerValidator).validateInUse(locker);
			verify(locker).returnLocker();
			verify(lockerLogWriter).logAdminRelease(locker, admin);
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

			verify(locker, never()).returnLocker();
			verify(lockerLogWriter, never()).logAdminRelease(any(Locker.class), any(User.class));
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
			verify(locker).enable();
			verify(lockerLogWriter).logEnable(locker, admin);
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

			verify(locker).returnLocker();
			verify(lockerLogWriter).logAdminRelease(locker, admin);

			verify(locker).disable();
			verify(lockerLogWriter).logDisable(locker, admin);
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

			verify(locker, never()).returnLocker();
			verify(lockerLogWriter, never()).logAdminRelease(any(Locker.class), any(User.class));

			verify(locker).disable();
			verify(lockerLogWriter).logDisable(locker, admin);
		}
	}

	@Nested
	@DisplayName("releaseExpiredLocker")
	class ReleaseExpiredLocker {

		@Test
		@DisplayName("성공: 만료된 모든 사물함을 회수하고 로그를 남긴다")
		void givenExpiredLockers_whenReleaseExpiredLocker_thenReturnsAllAndLogs() {
			// given
			String adminId = "admin-1";

			User admin = createUser(adminId);
			LockerLocation location = createLocation("loc-1", LockerName.SECOND);
			Locker locker1 = createLocker("locker-1", 1L, location, createUser("user-1"),
				LocalDateTime.now().minusDays(1), true);
			Locker locker2 = createLocker("locker-2", 2L, location, createUser("user-2"),
				LocalDateTime.now().minusDays(1), true);

			when(userReader.findAdminUserById(adminId)).thenReturn(admin);
			when(lockerReader.findExpiredLockers(any(LocalDateTime.class)))
				.thenReturn(List.of(locker1, locker2));

			// when
			lockerAdminService.releaseExpiredLocker(adminId);

			// then
			verify(lockerReader).findExpiredLockers(any(LocalDateTime.class));

			verify(locker1).returnLocker();
			verify(locker2).returnLocker();

			verify(lockerLogWriter).logAdminRelease(locker1, admin);
			verify(lockerLogWriter).logAdminRelease(locker2, admin);
		}
	}
}
