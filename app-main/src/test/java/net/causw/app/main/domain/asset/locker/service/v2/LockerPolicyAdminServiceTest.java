package net.causw.app.main.domain.asset.locker.service.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.response.LockerPolicyResponse;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerPolicyReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerPolicyWriter;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerValidator;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.LockerErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("LockerPolicyAdminService 단위 테스트")
class LockerPolicyAdminServiceTest {

	@InjectMocks
	private LockerPolicyAdminService lockerPolicyAdminService;

	@Mock
	private LockerPolicyReader lockerPolicyReader;
	@Mock
	private LockerPolicyWriter lockerPolicyWriter;
	@Mock
	private LockerValidator lockerValidator;

	@Nested
	@DisplayName("getPolicy")
	class GetPolicy {

		@Test
		@DisplayName("성공: 리더에서 조회한 값을 기반으로 정책 응답을 생성한다")
		void givenExistingPolicy_whenGetPolicy_thenBuildsResponseFromReader() {
			// given
			LocalDateTime expireDate = LocalDateTime.now().plusDays(10);
			LocalDateTime registerStart = LocalDateTime.now().minusDays(5);
			LocalDateTime registerEnd = LocalDateTime.now().plusDays(5);
			LocalDateTime extendStart = LocalDateTime.now().plusDays(6);
			LocalDateTime extendEnd = LocalDateTime.now().plusDays(9);
			LocalDateTime nextExpire = LocalDateTime.now().plusDays(40);

			when(lockerPolicyReader.findExpireDateOptional()).thenReturn(java.util.Optional.of(expireDate));
			when(lockerPolicyReader.findRegisterStartDate()).thenReturn(java.util.Optional.of(registerStart));
			when(lockerPolicyReader.findRegisterEndDate()).thenReturn(java.util.Optional.of(registerEnd));
			when(lockerPolicyReader.findExtendStartDate()).thenReturn(java.util.Optional.of(extendStart));
			when(lockerPolicyReader.findExtendEndDate()).thenReturn(java.util.Optional.of(extendEnd));
			when(lockerPolicyReader.findNextExpireDateOptional()).thenReturn(java.util.Optional.of(nextExpire));
			when(lockerPolicyReader.getLockerAccessStatusFlag()).thenReturn(true);
			when(lockerPolicyReader.getLockerExtendStatusFlag()).thenReturn(false);

			// when
			LockerPolicyResponse response = lockerPolicyAdminService.getPolicy();

			// then
			assertThat(response.expiredAt()).isEqualTo(expireDate);
			assertThat(response.registerStartAt()).isEqualTo(registerStart);
			assertThat(response.registerEndAt()).isEqualTo(registerEnd);
			assertThat(response.extendStartAt()).isEqualTo(extendStart);
			assertThat(response.extendEndAt()).isEqualTo(extendEnd);
			assertThat(response.nextExpiredAt()).isEqualTo(nextExpire);
			assertThat(response.isLockerAccessEnabled()).isTrue();
			assertThat(response.isLockerExtendEnabled()).isFalse();
		}

		@Test
		@DisplayName("성공: 값이 없는 경우 null로 채워진 정책 응답을 생성한다")
		void givenEmptyPolicy_whenGetPolicy_thenBuildsResponseWithNulls() {
			// given
			when(lockerPolicyReader.findExpireDateOptional()).thenReturn(java.util.Optional.empty());
			when(lockerPolicyReader.findRegisterStartDate()).thenReturn(java.util.Optional.empty());
			when(lockerPolicyReader.findRegisterEndDate()).thenReturn(java.util.Optional.empty());
			when(lockerPolicyReader.findExtendStartDate()).thenReturn(java.util.Optional.empty());
			when(lockerPolicyReader.findExtendEndDate()).thenReturn(java.util.Optional.empty());
			when(lockerPolicyReader.findNextExpireDateOptional()).thenReturn(java.util.Optional.empty());
			when(lockerPolicyReader.getLockerAccessStatusFlag()).thenReturn(false);
			when(lockerPolicyReader.getLockerExtendStatusFlag()).thenReturn(false);

			// when
			LockerPolicyResponse response = lockerPolicyAdminService.getPolicy();

			// then
			assertThat(response.expiredAt()).isNull();
			assertThat(response.registerStartAt()).isNull();
			assertThat(response.registerEndAt()).isNull();
			assertThat(response.extendStartAt()).isNull();
			assertThat(response.extendEndAt()).isNull();
			assertThat(response.nextExpiredAt()).isNull();
			assertThat(response.isLockerAccessEnabled()).isFalse();
			assertThat(response.isLockerExtendEnabled()).isFalse();
		}
	}

	@Nested
	@DisplayName("updateRegisterPeriod")
	class UpdateRegisterPeriod {

		@Test
		@DisplayName("성공: 신청 기간과 만료일을 업데이트 한다")
		void givenValidPeriod_whenUpdateRegisterPeriod_thenDelegatesToWriter() {
			// given
			LocalDateTime start = LocalDateTime.now();
			LocalDateTime end = LocalDateTime.now().plusDays(7);
			LocalDateTime expiredAt = LocalDateTime.now().plusDays(30);

			// when
			lockerPolicyAdminService.updateRegisterPeriod(start, end, expiredAt);

			// then
			verify(lockerPolicyWriter).updateRegisterPeriod(start, end, expiredAt);
		}

		@Test
		@DisplayName("실패: 시작일이 종료일보다 뒤이면 예외를 발생시킨다")
		void givenStartAfterEnd_whenUpdateRegisterPeriod_thenThrows() {
			// given
			LocalDateTime start = LocalDateTime.now().plusDays(7);
			LocalDateTime end = LocalDateTime.now();
			LocalDateTime expiredAt = LocalDateTime.now().plusDays(30);

			doThrow(LockerErrorCode.LOCKER_PERIOD_START_AFTER_END.toBaseException())
				.when(lockerValidator).validatePeriodOrder(start, end, expiredAt);

			// when & then
			assertThatThrownBy(() -> lockerPolicyAdminService.updateRegisterPeriod(start, end, expiredAt))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_PERIOD_START_AFTER_END.getMessage());
			verify(lockerPolicyWriter, never()).updateRegisterPeriod(start, end, expiredAt);
		}

		@Test
		@DisplayName("실패: 만료일이 종료일보다 앞이면 예외를 발생시킨다")
		void givenExpireBeforeEnd_whenUpdateRegisterPeriod_thenThrows() {
			// given
			LocalDateTime start = LocalDateTime.now();
			LocalDateTime end = LocalDateTime.now().plusDays(7);
			LocalDateTime expiredAt = LocalDateTime.now().plusDays(5);

			doThrow(LockerErrorCode.LOCKER_PERIOD_END_AFTER_EXPIRE.toBaseException())
				.when(lockerValidator).validatePeriodOrder(start, end, expiredAt);

			// when & then
			assertThatThrownBy(() -> lockerPolicyAdminService.updateRegisterPeriod(start, end, expiredAt))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_PERIOD_END_AFTER_EXPIRE.getMessage());
			verify(lockerPolicyWriter, never()).updateRegisterPeriod(start, end, expiredAt);
		}
	}

	@Nested
	@DisplayName("updateExtendPeriod")
	class UpdateExtendPeriod {

		@Test
		@DisplayName("성공: 연장 기간과 다음 만료일을 업데이트 한다")
		void givenValidPeriod_whenUpdateExtendPeriod_thenDelegatesToWriter() {
			// given
			LocalDateTime start = LocalDateTime.now();
			LocalDateTime end = LocalDateTime.now().plusDays(7);
			LocalDateTime nextExpire = LocalDateTime.now().plusDays(60);

			// when
			lockerPolicyAdminService.updateExtendPeriod(start, end, nextExpire);

			// then
			verify(lockerPolicyWriter).updateExtendPeriod(start, end, nextExpire);
		}

		@Test
		@DisplayName("실패: 시작일이 종료일보다 뒤이면 예외를 발생시킨다")
		void givenStartAfterEnd_whenUpdateExtendPeriod_thenThrows() {
			// given
			LocalDateTime start = LocalDateTime.now().plusDays(7);
			LocalDateTime end = LocalDateTime.now();
			LocalDateTime nextExpire = LocalDateTime.now().plusDays(60);

			doThrow(LockerErrorCode.LOCKER_PERIOD_START_AFTER_END.toBaseException())
				.when(lockerValidator).validatePeriodOrder(start, end, nextExpire);

			// when & then
			assertThatThrownBy(() -> lockerPolicyAdminService.updateExtendPeriod(start, end, nextExpire))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_PERIOD_START_AFTER_END.getMessage());
			verify(lockerPolicyWriter, never()).updateExtendPeriod(start, end, nextExpire);
		}

		@Test
		@DisplayName("실패: 다음 만료일이 종료일보다 앞이면 예외를 발생시킨다")
		void givenNextExpireBeforeEnd_whenUpdateExtendPeriod_thenThrows() {
			// given
			LocalDateTime start = LocalDateTime.now();
			LocalDateTime end = LocalDateTime.now().plusDays(7);
			LocalDateTime nextExpire = LocalDateTime.now().plusDays(5);

			doThrow(LockerErrorCode.LOCKER_PERIOD_END_AFTER_EXPIRE.toBaseException())
				.when(lockerValidator).validatePeriodOrder(start, end, nextExpire);

			// when & then
			assertThatThrownBy(() -> lockerPolicyAdminService.updateExtendPeriod(start, end, nextExpire))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_PERIOD_END_AFTER_EXPIRE.getMessage());
			verify(lockerPolicyWriter, never()).updateExtendPeriod(start, end, nextExpire);
		}
	}

	@Nested
	@DisplayName("updateRegisterStatus")
	class UpdateRegisterStatus {

		@Test
		@DisplayName("성공: 연장이 비활성 상태일 때 신청 상태를 변경한다")
		void givenExtendInactive_whenUpdateRegisterStatus_thenDelegatesToWriter() {
			// given
			boolean newStatus = true;
			when(lockerPolicyReader.getLockerExtendStatusFlag()).thenReturn(false);

			// when
			lockerPolicyAdminService.updateRegisterStatus(newStatus);

			// then
			verify(lockerPolicyReader).getLockerExtendStatusFlag();
			verify(lockerPolicyWriter).updateRegisterStatus(newStatus);
		}

		@Test
		@DisplayName("실패: 연장이 이미 활성 상태이면 예외를 발생시킨다")
		void givenExtendActive_whenUpdateRegisterStatus_thenThrowsLockerExtendAlreadyActive() {
			// given
			when(lockerPolicyReader.getLockerExtendStatusFlag()).thenReturn(true);

			// when & then
			assertThatThrownBy(() -> lockerPolicyAdminService.updateRegisterStatus(true))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_EXTEND_ALREADY_ACTIVE.getMessage());
		}
	}

	@Nested
	@DisplayName("updateExtendStatus")
	class UpdateExtendStatus {

		@Test
		@DisplayName("성공: 신청이 비활성 상태일 때 연장 상태를 변경한다")
		void givenRegisterInactive_whenUpdateExtendStatus_thenDelegatesToWriter() {
			// given
			boolean newStatus = true;
			when(lockerPolicyReader.getLockerAccessStatusFlag()).thenReturn(false);

			// when
			lockerPolicyAdminService.updateExtendStatus(newStatus);

			// then
			verify(lockerPolicyReader).getLockerAccessStatusFlag();
			verify(lockerPolicyWriter).updateExtendStatus(newStatus);
		}

		@Test
		@DisplayName("실패: 신청이 이미 활성 상태이면 예외를 발생시킨다")
		void givenRegisterActive_whenUpdateExtendStatus_thenThrowsLockerRegisterAlreadyActive() {
			// given
			when(lockerPolicyReader.getLockerAccessStatusFlag()).thenReturn(true);

			// when & then
			assertThatThrownBy(() -> lockerPolicyAdminService.updateExtendStatus(true))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessage(LockerErrorCode.LOCKER_REGISTER_ALREADY_ACTIVE.getMessage());
		}
	}
}
