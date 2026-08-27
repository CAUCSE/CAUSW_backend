package net.causw.app.main.domain.asset.locker.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import net.causw.app.main.domain.admin.audit.event.AdminAuditLogEventPublisher;
import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerLocation;
import net.causw.app.main.domain.asset.locker.entity.LockerName;
import net.causw.app.main.domain.notification.notification.event.LockerExpiredEvent;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
@DisplayName("LockerExpirationProcessor 단위 테스트")
class LockerExpirationProcessorTest {

	@InjectMocks
	private LockerExpirationProcessor lockerExpirationProcessor;

	@Mock
	private LockerReader lockerReader;
	@Mock
	private LockerWriter lockerWriter;
	@Mock
	private AdminAuditLogEventPublisher adminAuditLogEventPublisher;
	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	private User createUser(String userId) {
		return ObjectFixtures.getCertifiedUserWithId(userId);
	}

	private Locker createLocker(String id, User user, LocalDateTime expiredAt) {
		LockerLocation location = ObjectFixtures.getLockerLocationWithId(LockerName.SECOND, "loc-1");
		return ObjectFixtures.getLockerWithId(id, 1L, true, user, location, expiredAt);
	}

	@Test
	@DisplayName("성공: 만료된 사물함을 반납하고 감사 로그와 알림 이벤트를 발행한다")
	void givenExpiredLocker_whenRelease_thenReleasesAndPublishesEvents() {
		// given
		User actor = createUser("actor-1");
		User user = createUser("user-1");
		Locker locker = createLocker("locker-1", user, LocalDateTime.now().minusDays(1));

		when(lockerReader.findByIdForWrite("locker-1")).thenReturn(locker);

		// when
		boolean released = lockerExpirationProcessor.releaseExpiredLocker("locker-1", actor);

		// then
		assertThat(released).isTrue();
		verify(lockerWriter).releaseLocker(locker, actor, user.getEmail(), user.getName());
		verify(adminAuditLogEventPublisher).publishLockerReleaseExpired(locker, actor, Optional.of(user));
		verify(applicationEventPublisher).publishEvent(new LockerExpiredEvent(user.getId(), locker.getId()));
	}

	@Test
	@DisplayName("성공: 사용자가 없는 만료 사물함은 알림 이벤트 없이 반납한다")
	void givenExpiredLockerWithoutUser_whenRelease_thenReleasesWithoutNotification() {
		// given
		User actor = createUser("actor-1");
		Locker locker = createLocker("locker-1", null, LocalDateTime.now().minusDays(1));

		when(lockerReader.findByIdForWrite("locker-1")).thenReturn(locker);

		// when
		boolean released = lockerExpirationProcessor.releaseExpiredLocker("locker-1", actor);

		// then
		assertThat(released).isTrue();
		verify(lockerWriter).releaseLocker(locker, actor, "알 수 없음", "알 수 없음");
		verify(adminAuditLogEventPublisher).publishLockerReleaseExpired(locker, actor, Optional.empty());
		verifyNoInteractions(applicationEventPublisher);
	}

	@Test
	@DisplayName("멱등성: 이미 반납된 사물함(만료일 없음)은 재처리하지 않는다")
	void givenAlreadyReturnedLocker_whenRelease_thenSkips() {
		// given
		User actor = createUser("actor-1");
		Locker locker = createLocker("locker-1", null, null);

		when(lockerReader.findByIdForWrite("locker-1")).thenReturn(locker);

		// when
		boolean released = lockerExpirationProcessor.releaseExpiredLocker("locker-1", actor);

		// then
		assertThat(released).isFalse();
		verifyNoInteractions(lockerWriter, adminAuditLogEventPublisher, applicationEventPublisher);
	}

	@Test
	@DisplayName("멱등성: 만료되지 않은 사물함은 처리하지 않는다")
	void givenNotExpiredLocker_whenRelease_thenSkips() {
		// given
		User actor = createUser("actor-1");
		User user = createUser("user-1");
		Locker locker = createLocker("locker-1", user, LocalDateTime.now().plusDays(1));

		when(lockerReader.findByIdForWrite("locker-1")).thenReturn(locker);

		// when
		boolean released = lockerExpirationProcessor.releaseExpiredLocker("locker-1", actor);

		// then
		assertThat(released).isFalse();
		verifyNoInteractions(lockerWriter, adminAuditLogEventPublisher, applicationEventPublisher);
	}
}
