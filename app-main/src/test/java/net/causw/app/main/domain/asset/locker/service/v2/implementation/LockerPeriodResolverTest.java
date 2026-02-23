package net.causw.app.main.domain.asset.locker.service.v2.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.asset.locker.enums.LockerPeriodPhase;
import net.causw.app.main.domain.asset.locker.service.v2.dto.result.LockerPeriodStatusResult;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.LockerErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("LockerPeriodResolver 단위 테스트")
class LockerPeriodResolverTest {

	@InjectMocks
	private LockerPeriodResolver lockerPeriodResolver;

	@Mock
	private LockerPolicyReader lockerPolicyReader;

	private static final LocalDateTime START = LocalDateTime.of(2026, 6, 1, 0, 0);
	private static final LocalDateTime END = LocalDateTime.of(2026, 6, 7, 23, 59);

	@Nested
	@DisplayName("사물함 신청 가능 여부(isRegisterActive)")
	class IsRegisterActive {

		@Test
		@DisplayName("신청 flag ON 이고 신청 기간 내이면 신청 가능이다")
		void givenFlagOnAndWithinPeriod_whenCheck_thenTrue() {
			// given
			LocalDateTime target = LocalDateTime.of(2026, 6, 3, 12, 0);
			when(lockerPolicyReader.getLockerAccessStatusFlag()).thenReturn(true);
			when(lockerPolicyReader.findRegisterStartDate()).thenReturn(Optional.of(START));
			when(lockerPolicyReader.findRegisterEndDate()).thenReturn(Optional.of(END));

			// when
			boolean result = lockerPeriodResolver.isRegisterActive(target);

			// then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("신청 flag OFF 이면 신청 불가능이다")
		void givenFlagOff_whenCheck_thenFalse() {
			// given
			LocalDateTime target = LocalDateTime.of(2026, 6, 3, 12, 0);
			when(lockerPolicyReader.getLockerAccessStatusFlag()).thenReturn(false);
			when(lockerPolicyReader.findRegisterStartDate()).thenReturn(Optional.of(START));
			when(lockerPolicyReader.findRegisterEndDate()).thenReturn(Optional.of(END));

			// when
			boolean result = lockerPeriodResolver.isRegisterActive(target);

			// then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("신청 flag ON 이어도 신청 기간 밖이면 신청 불가능이다")
		void givenFlagOnAndOutsidePeriod_whenCheck_thenFalse() {
			// given
			LocalDateTime target = LocalDateTime.of(2026, 7, 1, 12, 0);
			when(lockerPolicyReader.getLockerAccessStatusFlag()).thenReturn(true);
			when(lockerPolicyReader.findRegisterStartDate()).thenReturn(Optional.of(START));
			when(lockerPolicyReader.findRegisterEndDate()).thenReturn(Optional.of(END));

			// when
			boolean result = lockerPeriodResolver.isRegisterActive(target);

			// then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("신청 flag ON 이고 신청 기간이 설정되지 않았으면 false를 반환한다.")
		void givenFlagOnAndPeriodNotSet_whenCheck_thenThrow() {
			// given
			LocalDateTime target = LocalDateTime.of(2026, 6, 3, 12, 0);
			when(lockerPolicyReader.getLockerAccessStatusFlag()).thenReturn(true);
			when(lockerPolicyReader.findRegisterStartDate()).thenReturn(Optional.empty());
			when(lockerPolicyReader.findRegisterEndDate()).thenReturn(Optional.empty());

			// when & then
			assertThat(lockerPeriodResolver.isRegisterActive(target))
					.isFalse();
		}
	}

	@Nested
	@DisplayName("사물함 연장 가능 여부(isExtendActive)")
	class IsExtendActive {

		@Test
		@DisplayName("연장 flag ON 이고 연장 기간 내이면 연장 가능이다")
		void givenFlagOnAndWithinPeriod_whenCheck_thenTrue() {
			// given
			LocalDateTime target = LocalDateTime.of(2026, 6, 3, 12, 0);
			when(lockerPolicyReader.getLockerExtendStatusFlag()).thenReturn(true);
			when(lockerPolicyReader.findExtendStartDate()).thenReturn(Optional.of(START));
			when(lockerPolicyReader.findExtendEndDate()).thenReturn(Optional.of(END));

			// when
			boolean result = lockerPeriodResolver.isExtendActive(target);

			// then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("연장 flag OFF 이면 연장 불가능이다")
		void givenFlagOff_whenCheck_thenFalse() {
			// given
			LocalDateTime target = LocalDateTime.of(2026, 6, 3, 12, 0);
			when(lockerPolicyReader.getLockerExtendStatusFlag()).thenReturn(false);
			when(lockerPolicyReader.findExtendStartDate()).thenReturn(Optional.of(START));
			when(lockerPolicyReader.findExtendEndDate()).thenReturn(Optional.of(END));

			// when
			boolean result = lockerPeriodResolver.isExtendActive(target);

			// then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("연장 flag ON 이어도 연장 기간 밖이면 연장 불가능이다")
		void givenFlagOnAndOutsidePeriod_whenCheck_thenFalse() {
			// given
			LocalDateTime target = LocalDateTime.of(2026, 7, 1, 12, 0);
			when(lockerPolicyReader.getLockerExtendStatusFlag()).thenReturn(true);
			when(lockerPolicyReader.findExtendStartDate()).thenReturn(Optional.of(START));
			when(lockerPolicyReader.findExtendEndDate()).thenReturn(Optional.of(END));

			// when
			boolean result = lockerPeriodResolver.isExtendActive(target);

			// then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("연장 flag ON 이고 연장 기간이 설정되지 않았으면 false를 반환한다")
		void givenFlagOnAndPeriodNotSet_whenCheck_thenThrow() {
			// given
			LocalDateTime target = LocalDateTime.of(2026, 6, 3, 12, 0);
			when(lockerPolicyReader.getLockerExtendStatusFlag()).thenReturn(true);
			when(lockerPolicyReader.findExtendStartDate()).thenReturn(Optional.empty());
			when(lockerPolicyReader.findExtendEndDate()).thenReturn(Optional.empty());

			// when & then
			assertThat(lockerPeriodResolver.isExtendActive(target))
					.isFalse();
		}
	}

	@Nested
	@DisplayName("현재 사물함 기간 상태 판별(resolveCurrentPhase)")
	class ResolveCurrentPhase {

		@Test
		@DisplayName("신청 flag ON 이고 신청 기간 전이면 READY 상태를 반환한다")
		void givenAccessFlagOnAndBeforePeriod_whenResolve_thenReady() {
			// given
			LocalDateTime now = START.minusDays(1);
			when(lockerPolicyReader.getLockerAccessStatusFlag()).thenReturn(true);
			when(lockerPolicyReader.findRegisterStartDate()).thenReturn(Optional.of(START));
			when(lockerPolicyReader.findRegisterEndDate()).thenReturn(Optional.of(END));

			// when
			LockerPeriodStatusResult result = lockerPeriodResolver.resolveCurrentPhase(now);

			// then
			assertThat(result.phase()).isEqualTo(LockerPeriodPhase.READY);
			assertThat(result.startAt()).isEqualTo(START);
			assertThat(result.endAt()).isEqualTo(END);
		}

		@Test
		@DisplayName("신청 flag ON 이고 신청 기간 내이면 APPLY 상태를 반환한다")
		void givenAccessFlagOnAndWithinPeriod_whenResolve_thenApply() {
			// given
			LocalDateTime now = LocalDateTime.of(2026, 6, 3, 12, 0);
			when(lockerPolicyReader.getLockerAccessStatusFlag()).thenReturn(true);
			when(lockerPolicyReader.findRegisterStartDate()).thenReturn(Optional.of(START));
			when(lockerPolicyReader.findRegisterEndDate()).thenReturn(Optional.of(END));

			// when
			LockerPeriodStatusResult result = lockerPeriodResolver.resolveCurrentPhase(now);

			// then
			assertThat(result.phase()).isEqualTo(LockerPeriodPhase.APPLY);
			assertThat(result.startAt()).isEqualTo(START);
			assertThat(result.endAt()).isEqualTo(END);
		}

		@Test
		@DisplayName("신청 flag ON 이고 신청 기간 후이면 CLOSED 상태를 반환한다")
		void givenAccessFlagOnAndAfterPeriod_whenResolve_thenClosed() {
			// given
			LocalDateTime now = END.plusDays(1);
			when(lockerPolicyReader.getLockerAccessStatusFlag()).thenReturn(true);
			when(lockerPolicyReader.findRegisterStartDate()).thenReturn(Optional.of(START));
			when(lockerPolicyReader.findRegisterEndDate()).thenReturn(Optional.of(END));

			// when
			LockerPeriodStatusResult result = lockerPeriodResolver.resolveCurrentPhase(now);

			// then
			assertThat(result.phase()).isEqualTo(LockerPeriodPhase.CLOSED);
			assertThat(result.startAt()).isNull();
			assertThat(result.endAt()).isNull();
		}

		@Test
		@DisplayName("연장 flag ON 이고 연장 기간 내이면 EXTEND 상태를 반환한다")
		void givenExtendFlagOnAndWithinPeriod_whenResolve_thenExtend() {
			// given
			LocalDateTime now = LocalDateTime.of(2026, 6, 3, 12, 0);
			when(lockerPolicyReader.getLockerAccessStatusFlag()).thenReturn(false);
			when(lockerPolicyReader.getLockerExtendStatusFlag()).thenReturn(true);
			when(lockerPolicyReader.findExtendStartDate()).thenReturn(Optional.of(START));
			when(lockerPolicyReader.findExtendEndDate()).thenReturn(Optional.of(END));

			// when
			LockerPeriodStatusResult result = lockerPeriodResolver.resolveCurrentPhase(now);

			// then
			assertThat(result.phase()).isEqualTo(LockerPeriodPhase.EXTEND);
			assertThat(result.startAt()).isEqualTo(START);
			assertThat(result.endAt()).isEqualTo(END);
		}

		@Test
		@DisplayName("신청/연장 flag가 모두 OFF 이면 CLOSED 상태를 반환한다")
		void givenBothFlagsOff_whenResolve_thenClosed() {
			// given
			LocalDateTime now = LocalDateTime.of(2026, 6, 3, 12, 0);
			when(lockerPolicyReader.getLockerAccessStatusFlag()).thenReturn(false);
			when(lockerPolicyReader.getLockerExtendStatusFlag()).thenReturn(false);

			// when
			LockerPeriodStatusResult result = lockerPeriodResolver.resolveCurrentPhase(now);

			// then
			assertThat(result.phase()).isEqualTo(LockerPeriodPhase.CLOSED);
			assertThat(result.startAt()).isNull();
			assertThat(result.endAt()).isNull();
		}

		@Test
		@DisplayName("flag ON 이지만 기간이 설정되지 않으면 CLOSED 상태를 반환한다")
		void givenFlagOnAndPeriodNotSet_whenResolve_thenClosed() {
			// given
			LocalDateTime now = LocalDateTime.of(2026, 6, 3, 12, 0);
			when(lockerPolicyReader.getLockerAccessStatusFlag()).thenReturn(true);
			when(lockerPolicyReader.findRegisterStartDate()).thenReturn(Optional.empty());
			when(lockerPolicyReader.findRegisterEndDate()).thenReturn(Optional.empty());

			// when
			LockerPeriodStatusResult result = lockerPeriodResolver.resolveCurrentPhase(now);

			// then
			assertThat(result.phase()).isEqualTo(LockerPeriodPhase.CLOSED);
			assertThat(result.startAt()).isNull();
			assertThat(result.endAt()).isNull();
		}
	}
}
