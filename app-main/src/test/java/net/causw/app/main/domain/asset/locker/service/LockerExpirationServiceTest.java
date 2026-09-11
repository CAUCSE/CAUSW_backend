package net.causw.app.main.domain.asset.locker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerLocation;
import net.causw.app.main.domain.asset.locker.entity.LockerName;
import net.causw.app.main.domain.asset.locker.service.dto.result.LockerExpirationResult;
import net.causw.app.main.domain.asset.locker.service.implementation.LockerExpirationProcessor;
import net.causw.app.main.domain.asset.locker.service.implementation.LockerReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
@DisplayName("LockerExpirationService 단위 테스트")
class LockerExpirationServiceTest {

	@InjectMocks
	private LockerExpirationService lockerExpirationService;

	@Mock
	private LockerReader lockerReader;
	@Mock
	private LockerExpirationProcessor lockerExpirationProcessor;

	private User createUser(String userId) {
		return ObjectFixtures.getCertifiedUserWithId(userId);
	}

	private Locker createLocker(String id, User user) {
		LockerLocation location = ObjectFixtures.getLockerLocationWithId(LockerName.SECOND, "loc-1");
		return ObjectFixtures.getLockerWithId(id, 1L, true, user, location, LocalDateTime.now().minusDays(1));
	}

	@Test
	@DisplayName("성공: 만료된 모든 사물함을 사물함 단위로 반납 처리한다")
	void givenExpiredLockers_whenReleaseExpiredLockers_thenReleasesAll() {
		// given
		User actor = createUser("actor-1");
		Locker locker1 = createLocker("locker-1", createUser("user-1"));
		Locker locker2 = createLocker("locker-2", createUser("user-2"));

		when(lockerReader.findExpiredLockers(any(LocalDateTime.class)))
			.thenReturn(List.of(locker1, locker2));
		when(lockerExpirationProcessor.releaseExpiredLocker(anyString(), eq(actor))).thenReturn(true);

		// when
		LockerExpirationResult result = lockerExpirationService.releaseExpiredLockers(actor);

		// then
		assertThat(result).isEqualTo(new LockerExpirationResult(2, 0, 0));
		verify(lockerExpirationProcessor).releaseExpiredLocker("locker-1", actor);
		verify(lockerExpirationProcessor).releaseExpiredLocker("locker-2", actor);
	}

	@Test
	@DisplayName("멱등성: 처리 시점에 이미 반납된 사물함은 스킵 건수로 집계한다")
	void givenAlreadyReturnedLocker_whenReleaseExpiredLockers_thenCountsAsSkipped() {
		// given
		User actor = createUser("actor-1");
		Locker locker1 = createLocker("locker-1", createUser("user-1"));
		Locker locker2 = createLocker("locker-2", createUser("user-2"));

		when(lockerReader.findExpiredLockers(any(LocalDateTime.class)))
			.thenReturn(List.of(locker1, locker2));
		when(lockerExpirationProcessor.releaseExpiredLocker("locker-1", actor)).thenReturn(true);
		when(lockerExpirationProcessor.releaseExpiredLocker("locker-2", actor)).thenReturn(false);

		// when
		LockerExpirationResult result = lockerExpirationService.releaseExpiredLockers(actor);

		// then
		assertThat(result).isEqualTo(new LockerExpirationResult(1, 1, 0));
	}

	@Test
	@DisplayName("실패 격리: 한 건이 실패해도 나머지 사물함은 계속 처리한다")
	void givenProcessorFailure_whenReleaseExpiredLockers_thenContinuesRemaining() {
		// given
		User actor = createUser("actor-1");
		Locker locker1 = createLocker("locker-1", createUser("user-1"));
		Locker locker2 = createLocker("locker-2", createUser("user-2"));

		when(lockerReader.findExpiredLockers(any(LocalDateTime.class)))
			.thenReturn(List.of(locker1, locker2));
		when(lockerExpirationProcessor.releaseExpiredLocker("locker-1", actor))
			.thenThrow(new RuntimeException("DB 오류"));
		when(lockerExpirationProcessor.releaseExpiredLocker("locker-2", actor)).thenReturn(true);

		// when
		LockerExpirationResult result = lockerExpirationService.releaseExpiredLockers(actor);

		// then
		assertThat(result).isEqualTo(new LockerExpirationResult(1, 0, 1));
		verify(lockerExpirationProcessor).releaseExpiredLocker("locker-2", actor);
	}

	@Test
	@DisplayName("성공: 만료된 사물함이 없으면 아무것도 처리하지 않는다")
	void givenNoExpiredLockers_whenReleaseExpiredLockers_thenDoesNothing() {
		// given
		User actor = createUser("actor-1");

		when(lockerReader.findExpiredLockers(any(LocalDateTime.class))).thenReturn(List.of());

		// when
		LockerExpirationResult result = lockerExpirationService.releaseExpiredLockers(actor);

		// then
		assertThat(result).isEqualTo(new LockerExpirationResult(0, 0, 0));
		verifyNoInteractions(lockerExpirationProcessor);
	}
}
