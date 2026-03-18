package net.causw.app.main.domain.asset.locker.service.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerLocation;
import net.causw.app.main.domain.asset.locker.entity.LockerName;
import net.causw.app.main.domain.asset.locker.enums.LockerPeriodPhase;
import net.causw.app.main.domain.asset.locker.repository.dto.LockerCountByLocation;
import net.causw.app.main.domain.asset.locker.service.v2.dto.result.LockerFloorListResult;
import net.causw.app.main.domain.asset.locker.service.v2.dto.result.LockerLocationResult;
import net.causw.app.main.domain.asset.locker.service.v2.dto.result.LockerPeriodStatusResult;
import net.causw.app.main.domain.asset.locker.service.v2.dto.result.MyLockerResult;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerLocationReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerPeriodResolver;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerPolicyReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerValidator;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.LockerErrorCode;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
@DisplayName("LockerService 단위 테스트")
class LockerServiceTest {

	@InjectMocks
	private LockerService lockerService;

	@Mock
	private LockerReader lockerReader;
	@Mock
	private LockerLocationReader lockerLocationReader;
	@Mock
	private LockerPolicyReader lockerPolicyReader;
	@Mock
	private LockerPeriodResolver lockerPeriodResolver;
	@Mock
	private LockerValidator lockerValidator;
	@Mock
	private LockerWriter lockerWriter;
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
	@DisplayName("registerLocker")
	class RegisterLocker {

		@Test
		@DisplayName("성공: 기존 사물함이 없는 경우 신규 신청")
		void givenUserWithoutLocker_whenRegisterLocker_thenRegistersNewLocker() {
			// given
			String lockerId = "locker-1";
			String userId = "user-1";

			User user = createUser(userId);
			LockerLocation location = createLocation("loc-1", LockerName.SECOND);
			Locker locker = createLocker("locker-1", 1L, location, null, null, true);
			LocalDateTime expiredAt = LocalDateTime.now().plusDays(30);

			when(userReader.findUserById(userId)).thenReturn(user);
			when(lockerReader.findByIdForWrite(lockerId)).thenReturn(locker);
			when(lockerReader.findByUserId(userId)).thenReturn(Optional.empty());
			when(lockerPolicyReader.findExpireDate()).thenReturn(expiredAt);

			// when
			lockerService.registerLocker(lockerId, userId);

			// then
			verify(lockerValidator).validateRegisterPeriod(any(LocalDateTime.class));
			verify(lockerValidator).validateRegisterAvailable(locker);
			verify(lockerReader).findByUserId(userId);

			verify(lockerWriter).registerLocker(locker, user, expiredAt);
			verify(lockerWriter, never()).returnLocker(any(Locker.class), any(User.class));
		}

		@Test
		@DisplayName("성공: 기존 사물함이 있는 경우 자동 반납 후 신청")
		void givenUserWithExistingLocker_whenRegisterLocker_thenReturnAndRegisterNewLocker() {
			// given
			String lockerId = "locker-2";
			String userId = "user-2";

			User user = createUser(userId);
			LockerLocation location = createLocation("loc-1", LockerName.SECOND);
			Locker existingLocker = createLocker("locker-existing", 1L, location, user, LocalDateTime.now().plusDays(1),
				true);
			Locker newLocker = createLocker("locker-new", 2L, location, null, null, true);
			LocalDateTime expiredAt = LocalDateTime.now().plusDays(30);

			when(userReader.findUserById(userId)).thenReturn(user);
			when(lockerReader.findByIdForWrite(lockerId)).thenReturn(newLocker);
			when(lockerReader.findByUserId(userId)).thenReturn(Optional.of(existingLocker));
			when(lockerPolicyReader.findExpireDate()).thenReturn(expiredAt);

			// when
			lockerService.registerLocker(lockerId, userId);

			// then
			verify(lockerWriter).returnLocker(existingLocker, user);
			verify(lockerWriter).registerLocker(newLocker, user, expiredAt);
		}

		@Test
		@DisplayName("실패: 신청 기간이 아니면 예외를 그대로 전달한다")
		void givenNotInRegisterPeriod_whenRegisterLocker_thenThrowsLockerRegisterNotAllowed() {
			// given
			String lockerId = "locker-1";
			String userId = "user-1";

			User user = createUser(userId);
			when(userReader.findUserById(userId)).thenReturn(user);

			BaseRunTimeV2Exception exception = LockerErrorCode.LOCKER_REGISTER_NOT_ALLOWED.toBaseException();
			doThrow(exception)
				.when(lockerValidator)
				.validateRegisterPeriod(any(LocalDateTime.class));

			// when & then
			assertThatThrownBy(() -> lockerService.registerLocker(lockerId, userId))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_REGISTER_NOT_ALLOWED.getMessage());

			// 검증 실패 시 이후 로직이 실행되지 않음을 보장
			verify(lockerReader, never()).findByIdForWrite(anyString());
		}
	}

	@Nested
	@DisplayName("returnLocker")
	class ReturnLocker {

		@Test
		@DisplayName("성공: 사용 중인 사물함을 소유자가 반납")
		void givenInUseLockerOwnedByUser_whenReturnLocker_thenReturnsLockerAndLogs() {
			// given
			String lockerId = "locker-1";
			String userId = "user-1";

			User user = createUser(userId);
			LockerLocation location = createLocation("loc-1", LockerName.SECOND);
			Locker locker = createLocker("locker-1", 1L, location, user, LocalDateTime.now().plusDays(1), true);

			when(userReader.findUserById(userId)).thenReturn(user);
			when(lockerReader.findByIdForWrite(lockerId)).thenReturn(locker);

			// when
			lockerService.returnLocker(lockerId, userId);

			// then
			verify(lockerValidator).validateReturnPeriod(any(LocalDateTime.class));
			verify(lockerValidator).validateInUse(locker);
			verify(lockerValidator).validateOwner(locker, user);

			verify(lockerWriter).returnLocker(locker, user);
		}

		@Test
		@DisplayName("실패: 반납 기간이 아니면 예외를 그대로 전달한다")
		void givenNotInReturnPeriod_whenReturnLocker_thenThrowsLockerReturnNotAllowed() {
			// given
			String lockerId = "locker-1";
			String userId = "user-1";

			User user = createUser(userId);
			when(userReader.findUserById(userId)).thenReturn(user);

			BaseRunTimeV2Exception exception = LockerErrorCode.LOCKER_RETURN_NOT_ALLOWED.toBaseException();
			doThrow(exception)
				.when(lockerValidator)
				.validateReturnPeriod(any(LocalDateTime.class));

			// when & then
			assertThatThrownBy(() -> lockerService.returnLocker(lockerId, userId))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_RETURN_NOT_ALLOWED.getMessage());

			verify(lockerReader, never()).findByIdForWrite(anyString());
		}
	}

	@Nested
	@DisplayName("extendLocker")
	class ExtendLocker {

		@Test
		@DisplayName("성공: 연장 가능 기간에 사물함 만료일 연장")
		void givenExtendableLocker_whenExtendLocker_thenExtendsExpireDateAndLogs() {
			// given
			String lockerId = "locker-1";
			String userId = "user-1";

			User user = createUser(userId);
			LockerLocation location = createLocation("loc-1", LockerName.SECOND);
			Locker locker = createLocker("locker-1", 1L, location, user, LocalDateTime.now().plusDays(1), true);
			LocalDateTime nextExpireDate = LocalDateTime.now().plusDays(30);

			when(userReader.findUserById(userId)).thenReturn(user);
			when(lockerReader.findByIdForWrite(lockerId)).thenReturn(locker);
			when(lockerPolicyReader.findNextExpireDate()).thenReturn(nextExpireDate);

			// when
			lockerService.extendLocker(lockerId, userId);

			// then
			verify(lockerValidator).validateExtendPeriod(any(LocalDateTime.class));
			verify(lockerValidator).validateInUse(locker);
			verify(lockerValidator).validateOwner(locker, user);
			verify(lockerValidator).validateNotAlreadyExtended(locker, nextExpireDate);

			verify(lockerWriter).extendLocker(locker, user, nextExpireDate);
		}

		@Test
		@DisplayName("실패: 연장 불가능 기간이면 예외를 그대로 전달한다")
		void givenNotInExtendPeriod_whenExtendLocker_thenThrowsLockerExtendNotAllowed() {
			// given
			String lockerId = "locker-1";
			String userId = "user-1";

			User user = createUser(userId);
			when(userReader.findUserById(userId)).thenReturn(user);

			BaseRunTimeV2Exception exception = LockerErrorCode.LOCKER_EXTEND_NOT_ALLOWED.toBaseException();
			doThrow(exception)
				.when(lockerValidator)
				.validateExtendPeriod(any(LocalDateTime.class));

			// when & then
			assertThatThrownBy(() -> lockerService.extendLocker(lockerId, userId))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_EXTEND_NOT_ALLOWED.getMessage());

			verify(lockerReader, never()).findByIdForWrite(anyString());
		}
	}

	@Nested
	@DisplayName("findCurrentPeriodStatus")
	class FindCurrentPeriodStatus {

		@Test
		@DisplayName("성공: 기간 리졸버에 위임하여 현재 상태를 조회한다")
		void givenCurrentTime_whenFindCurrentPeriodStatus_thenDelegatesToResolver() {
			// given
			LockerPeriodStatusResult expected = new LockerPeriodStatusResult(
				LockerPeriodPhase.APPLY,
				LocalDateTime.now().minusDays(1),
				LocalDateTime.now().plusDays(1));

			when(lockerPeriodResolver.resolveCurrentPhase(any(LocalDateTime.class))).thenReturn(expected);

			// when
			LockerPeriodStatusResult result = lockerService.findCurrentPeriodStatus();

			// then
			assertThat(result).isEqualTo(expected);
			verify(lockerPeriodResolver).resolveCurrentPhase(any(LocalDateTime.class));
		}
	}

	@Nested
	@DisplayName("findMyLocker")
	class FindMyLocker {

		@Test
		@DisplayName("성공: 사물함이 없으면 empty 결과를 반환한다")
		void givenUserWithoutLocker_whenFindMyLocker_thenReturnsEmptyResult() {
			// given
			String userId = "user-1";
			User user = createUser(userId);

			when(userReader.findUserById(userId)).thenReturn(user);
			when(lockerReader.findByUserId(anyString())).thenReturn(Optional.empty());

			// when
			MyLockerResult result = lockerService.findMyLocker(userId);

			// then
			assertThat(result.hasLocker()).isFalse();
		}
	}

	@Nested
	@DisplayName("findAllFloors / findByLocation (기본 동작 위임 확인)")
	class FindFloorsAndLocation {

		@Test
		@DisplayName("성공: 전체 층 조회 시 리더를 통해 데이터를 조회한다")
		void givenNothing_whenFindAllFloors_thenUsesReadersAndBuildsResult() {
			// given
			LockerLocation location = mock(LockerLocation.class);
			when(location.getId()).thenReturn("loc-1");

			when(lockerLocationReader.findAll()).thenReturn(List.of(location));
			when(lockerReader.countGroupByLocation())
				.thenReturn(Map.of("loc-1", new LockerCountByLocation("loc-1", 10, 3)));

			// when
			LockerFloorListResult result = lockerService.findAllFloors();

			// then (리더 호출 여부와 결과 객체 생성 여부만 간단히 확인)
			verify(lockerLocationReader).findAll();
			verify(lockerReader).countGroupByLocation();
			assertThat(result).isNotNull();
		}

		@Test
		@DisplayName("성공: 특정 위치 조회 시 리더를 통해 데이터를 조회한다")
		void givenLocationAndUser_whenFindByLocation_thenUsesReadersAndResolver() {
			// given
			String locationId = "loc-1";
			String userId = "user-1";

			LockerLocation location = mock(LockerLocation.class);
			Locker locker = mock(Locker.class);

			when(lockerLocationReader.findById(locationId)).thenReturn(location);
			when(lockerReader.findByLocationIdWithUser(locationId)).thenReturn(List.of(locker));
			when(lockerPeriodResolver.isRegisterActive(any(LocalDateTime.class))).thenReturn(true);
			when(lockerPeriodResolver.isExtendActive(any(LocalDateTime.class))).thenReturn(false);

			// when
			LockerLocationResult result = lockerService.findByLocation(locationId, userId);

			// then
			verify(lockerLocationReader).findById(locationId);
			verify(lockerReader).findByLocationIdWithUser(locationId);
			verify(lockerPeriodResolver).isRegisterActive(any(LocalDateTime.class));
			verify(lockerPeriodResolver).isExtendActive(any(LocalDateTime.class));
			assertThat(result).isNotNull();
		}
	}
}
