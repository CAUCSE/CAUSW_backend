package net.causw.app.main.domain.asset.locker.service.v2.implementation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerLocation;
import net.causw.app.main.domain.asset.locker.entity.LockerName;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.LockerErrorCode;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
@DisplayName("LockerValidator 단위 테스트")
class LockerValidatorTest {

	@InjectMocks
	private LockerValidator lockerValidator;

	@Mock
	private LockerPeriodResolver lockerPeriodResolver;
	@Mock
	private LockerReader lockerReader;

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 1, 12, 0);

	private User createUser(String userId) {
		return ObjectFixtures.getCertifiedUserWithId(userId);
	}

	private LockerLocation createLocation() {
		return ObjectFixtures.getLockerLocationWithId(LockerName.SECOND, "location-1");
	}

	private Locker createLocker(String id, User user, boolean isActive, LocalDateTime expireDate) {
		return Mockito.spy(ObjectFixtures.getLockerWithId(id, 1L, isActive, user, createLocation(), expireDate));
	}

	@Nested
	@DisplayName("사물함 신청 기간 검증(validateRegisterPeriod)")
	class ValidateRegisterPeriod {

		@Test
		@DisplayName("신청 기간이 활성 상태이면 예외 없이 통과한다")
		void givenActiveRegisterPeriod_whenValidate_thenPass() {
			// given
			when(lockerPeriodResolver.isRegisterActive(NOW)).thenReturn(true);

			// when & then
			assertThatCode(() -> lockerValidator.validateRegisterPeriod(NOW))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("신청 기간이 비활성 상태이면 LOCKER_REGISTER_NOT_ALLOWED 예외를 던진다")
		void givenInactiveRegisterPeriod_whenValidate_thenThrow() {
			// given
			when(lockerPeriodResolver.isRegisterActive(NOW)).thenReturn(false);

			// when & then
			assertThatThrownBy(() -> lockerValidator.validateRegisterPeriod(NOW))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_REGISTER_NOT_ALLOWED.getMessage());
		}
	}

	@Nested
	@DisplayName("사물함 반납 기간 검증(validateReturnPeriod)")
	class ValidateReturnPeriod {

		@Test
		@DisplayName("반납 가능 기간이면 예외 없이 통과한다")
		void givenActiveReturnPeriod_whenValidate_thenPass() {
			// given
			when(lockerPeriodResolver.isRegisterActive(NOW)).thenReturn(true);

			// when & then
			assertThatCode(() -> lockerValidator.validateReturnPeriod(NOW))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("반납 가능 기간이 아니면 LOCKER_RETURN_NOT_ALLOWED 예외를 던진다")
		void givenInactiveReturnPeriod_whenValidate_thenThrow() {
			// given
			when(lockerPeriodResolver.isRegisterActive(NOW)).thenReturn(false);

			// when & then
			assertThatThrownBy(() -> lockerValidator.validateReturnPeriod(NOW))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_RETURN_NOT_ALLOWED.getMessage());
		}
	}

	@Nested
	@DisplayName("사물함 연장 기간 검증(validateExtendPeriod)")
	class ValidateExtendPeriod {

		@Test
		@DisplayName("연장 가능 기간이면 예외 없이 통과한다")
		void givenActiveExtendPeriod_whenValidate_thenPass() {
			// given
			when(lockerPeriodResolver.isExtendActive(NOW)).thenReturn(true);

			// when & then
			assertThatCode(() -> lockerValidator.validateExtendPeriod(NOW))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("연장 가능 기간이 아니면 LOCKER_EXTEND_NOT_ALLOWED 예외를 던진다")
		void givenInactiveExtendPeriod_whenValidate_thenThrow() {
			// given
			when(lockerPeriodResolver.isExtendActive(NOW)).thenReturn(false);

			// when & then
			assertThatThrownBy(() -> lockerValidator.validateExtendPeriod(NOW))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_EXTEND_NOT_ALLOWED.getMessage());
		}
	}

	@Nested
	@DisplayName("유저용 사물함 신청 가능 상태 검증(validateRegisterAvailable)")
	class ValidateRegisterAvailable {

		@Test
		@DisplayName("사물함이 비어 있고 활성 상태(AVAILABLE)이면 신청 가능하다")
		void givenAvailableLocker_whenValidate_thenPass() {
			// given
			Locker locker = createLocker("locker-1", null, true, null);

			// when & then
			assertThatCode(() -> lockerValidator.validateRegisterAvailable(locker))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("사물함이 사용중(IN_USE)이면 LOCKER_IN_USE 예외를 던진다")
		void givenInUseLocker_whenValidate_thenThrowInUse() {
			// given
			User user = createUser("user-1");
			Locker locker = createLocker("locker-1", user, true, NOW.plusDays(30));

			// when & then
			assertThatThrownBy(() -> lockerValidator.validateRegisterAvailable(locker))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_IN_USE.getMessage());
		}

		@Test
		@DisplayName("사물함이 비활성(DISABLED)이면 LOCKER_DISABLED 예외를 던진다")
		void givenDisabledLocker_whenValidate_thenThrowDisabled() {
			// given
			Locker locker = createLocker("locker-1", null, false, null);

			// when & then
			assertThatThrownBy(() -> lockerValidator.validateRegisterAvailable(locker))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_DISABLED.getMessage());
		}
	}

	@Nested
	@DisplayName("사물함 소유자 검증(validateOwner)")
	class ValidateOwner {

		@Test
		@DisplayName("요청 유저가 사물함 소유자이면 예외 없이 통과한다")
		void givenMatchingOwner_whenValidate_thenPass() {
			// given
			User owner = createUser("user-1");
			Locker locker = createLocker("locker-1", owner, true, NOW.plusDays(30));

			// when & then
			assertThatCode(() -> lockerValidator.validateOwner(locker, owner))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("요청 유저가 사물함 소유자가 아니면 LOCKER_NOT_OWNER 예외를 던진다")
		void givenDifferentUser_whenValidate_thenThrow() {
			// given
			User owner = createUser("user-1");
			User other = createUser("user-2");
			Locker locker = createLocker("locker-1", owner, true, NOW.plusDays(30));

			// when & then
			assertThatThrownBy(() -> lockerValidator.validateOwner(locker, other))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_NOT_OWNER.getMessage());
		}
	}

	@Nested
	@DisplayName("사물함 이미 연장 여부 검증(validateNotAlreadyExtended)")
	class ValidateNotAlreadyExtended {

		@Test
		@DisplayName("현재 만료일과 연장 목표 만료일이 다르면 예외 없이 통과한다")
		void givenDifferentExpireDate_whenValidate_thenPass() {
			// given
			LocalDateTime currentExpire = NOW.plusDays(30);
			LocalDateTime nextExpire = NOW.plusDays(60);
			Locker locker = createLocker("locker-1", createUser("user-1"), true, currentExpire);

			// when & then
			assertThatCode(() -> lockerValidator.validateNotAlreadyExtended(locker, nextExpire))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("현재 만료일이 null 이면 예외 없이 통과한다")
		void givenNullExpireDate_whenValidate_thenPass() {
			// given
			LocalDateTime nextExpire = NOW.plusDays(60);
			Locker locker = createLocker("locker-1", createUser("user-1"), true, null);

			// when & then
			assertThatCode(() -> lockerValidator.validateNotAlreadyExtended(locker, nextExpire))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("현재 만료일과 연장 목표 만료일이 같으면 LOCKER_ALREADY_EXTENDED 예외를 던진다")
		void givenSameExpireDate_whenValidate_thenThrow() {
			// given
			LocalDateTime expireDate = NOW.plusDays(60);
			Locker locker = createLocker("locker-1", createUser("user-1"), true, expireDate);

			// when & then
			assertThatThrownBy(() -> lockerValidator.validateNotAlreadyExtended(locker, expireDate))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_ALREADY_EXTENDED.getMessage());
		}
	}

	@Nested
	@DisplayName("정책 기간 선후관계 검증(validatePeriodOrder)")
	class ValidatePeriodOrder {

		@Test
		@DisplayName("start < end < expiredAt 순서를 만족하면 예외 없이 통과한다")
		void givenValidOrder_whenValidate_thenPass() {
			// given
			LocalDateTime start = NOW;
			LocalDateTime end = NOW.plusDays(7);
			LocalDateTime expired = NOW.plusDays(30);

			// when & then
			assertThatCode(() -> lockerValidator.validatePeriodOrder(start, end, expired))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("start >= end 이면 LOCKER_PERIOD_START_AFTER_END 예외를 던진다")
		void givenStartAfterEnd_whenValidate_thenThrow() {
			// given
			LocalDateTime start = NOW.plusDays(7);
			LocalDateTime end = NOW;
			LocalDateTime expired = NOW.plusDays(30);

			// when & then
			assertThatThrownBy(() -> lockerValidator.validatePeriodOrder(start, end, expired))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_PERIOD_START_AFTER_END.getMessage());
		}

		@Test
		@DisplayName("end >= expiredAt 이면 LOCKER_PERIOD_END_AFTER_EXPIRE 예외를 던진다")
		void givenEndAfterExpired_whenValidate_thenThrow() {
			// given
			LocalDateTime start = NOW;
			LocalDateTime end = NOW.plusDays(30);
			LocalDateTime expired = NOW.plusDays(7);

			// when & then
			assertThatThrownBy(() -> lockerValidator.validatePeriodOrder(start, end, expired))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_PERIOD_END_AFTER_EXPIRE.getMessage());
		}
	}

	@Nested
	@DisplayName("관리자용 사물함 배정 가능 상태 검증(validateAssignable)")
	class ValidateAssignable {

		@Test
		@DisplayName("사물함이 AVAILABLE 이면 관리자 배정이 가능하다")
		void givenAvailableLocker_whenValidate_thenPass() {
			// given
			Locker locker = createLocker("locker-1", null, true, null);

			// when & then
			assertThatCode(() -> lockerValidator.validateAssignable(locker))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("사물함이 IN_USE 이면 LOCKER_NOT_AVAILABLE 예외를 던진다")
		void givenInUseLocker_whenValidate_thenThrow() {
			// given
			Locker locker = createLocker("locker-1", createUser("user-1"), true, NOW.plusDays(30));

			// when & then
			assertThatThrownBy(() -> lockerValidator.validateAssignable(locker))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_NOT_AVAILABLE.getMessage());
		}

		@Test
		@DisplayName("사물함이 DISABLED 이면 LOCKER_NOT_AVAILABLE 예외를 던진다")
		void givenDisabledLocker_whenValidate_thenThrow() {
			// given
			Locker locker = createLocker("locker-1", null, false, null);

			// when & then
			assertThatThrownBy(() -> lockerValidator.validateAssignable(locker))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_NOT_AVAILABLE.getMessage());
		}
	}

	@Nested
	@DisplayName("관리자용 유저 사물함 중복 보유 검증(validateUserNotHavingLocker)")
	class ValidateUserNotHavingLocker {

		@Test
		@DisplayName("유저가 사물함을 보유하지 않았다면 예외 없이 통과한다")
		void givenUserWithoutLocker_whenValidate_thenPass() {
			// given
			when(lockerReader.existsByUserId("user-1")).thenReturn(false);

			// when & then
			assertThatCode(() -> lockerValidator.validateUserNotHavingLocker("user-1"))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("유저가 이미 사물함을 보유했다면 LOCKER_USER_ALREADY_HAS_LOCKER 예외를 던진다")
		void givenUserWithLocker_whenValidate_thenThrow() {
			// given
			when(lockerReader.existsByUserId("user-1")).thenReturn(true);

			// when & then
			assertThatThrownBy(() -> lockerValidator.validateUserNotHavingLocker("user-1"))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_USER_ALREADY_HAS_LOCKER.getMessage());
		}
	}

	@Nested
	@DisplayName("사물함 사용중 상태 검증(validateInUse)")
	class ValidateInUse {

		@Test
		@DisplayName("사물함이 IN_USE 상태이면 예외 없이 통과한다")
		void givenInUseLocker_whenValidate_thenPass() {
			// given
			Locker locker = createLocker("locker-1", createUser("user-1"), true, NOW.plusDays(30));

			// when & then
			assertThatCode(() -> lockerValidator.validateInUse(locker))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("사물함이 AVAILABLE 이면 LOCKER_NOT_IN_USE 예외를 던진다")
		void givenAvailableLocker_whenValidate_thenThrow() {
			// given
			Locker locker = createLocker("locker-1", null, true, null);

			// when & then
			assertThatThrownBy(() -> lockerValidator.validateInUse(locker))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_NOT_IN_USE.getMessage());
		}

		@Test
		@DisplayName("사물함이 DISABLED 이면 LOCKER_NOT_IN_USE 예외를 던진다")
		void givenDisabledLocker_whenValidate_thenThrow() {
			// given
			Locker locker = createLocker("locker-1", null, false, null);

			// when & then
			assertThatThrownBy(() -> lockerValidator.validateInUse(locker))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_NOT_IN_USE.getMessage());
		}
	}

	@Nested
	@DisplayName("관리자용 사물함 활성화 가능 검증(validateEnableable)")
	class ValidateEnableable {

		@Test
		@DisplayName("사물함이 비활성 상태이면 활성화 가능하다")
		void givenInactiveLocker_whenValidate_thenPass() {
			// given
			Locker locker = createLocker("locker-1", null, false, null);

			// when & then
			assertThatCode(() -> lockerValidator.validateEnableable(locker))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("사물함이 이미 활성 상태이면 LOCKER_ALREADY_ACTIVE 예외를 던진다")
		void givenActiveLocker_whenValidate_thenThrow() {
			// given
			Locker locker = createLocker("locker-1", null, true, null);

			// when & then
			assertThatThrownBy(() -> lockerValidator.validateEnableable(locker))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_ALREADY_ACTIVE.getMessage());
		}
	}

	@Nested
	@DisplayName("관리자용 사물함 비활성화 가능 검증(validateDisableable)")
	class ValidateDisableable {

		@Test
		@DisplayName("사물함이 활성 상태이면 비활성화 가능하다")
		void givenActiveLocker_whenValidate_thenPass() {
			// given
			Locker locker = createLocker("locker-1", null, true, null);

			// when & then
			assertThatCode(() -> lockerValidator.validateDisableable(locker))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("사물함이 이미 비활성 상태이면 LOCKER_ALREADY_DISABLED 예외를 던진다")
		void givenInactiveLocker_whenValidate_thenThrow() {
			// given
			Locker locker = createLocker("locker-1", null, false, null);

			// when & then
			assertThatThrownBy(() -> lockerValidator.validateDisableable(locker))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_ALREADY_DISABLED.getMessage());
		}
	}
}
