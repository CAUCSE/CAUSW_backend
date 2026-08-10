package net.causw.app.main.core.security.batch;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.scheduling.annotation.Scheduled;

import net.causw.app.main.core.batch.BatchScheduler;
import net.causw.app.main.domain.asset.locker.service.LockerExpirationService;
import net.causw.app.main.domain.asset.locker.service.dto.result.LockerExpirationResult;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.UserProfileImageService;
import net.causw.app.main.domain.user.account.service.implementation.AdmissionWriter;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountWriter;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoWriter;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.shared.pageable.PageableFactory;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.InternalServerException;

@ExtendWith(MockitoExtension.class)
public class BatchSchedulerTest {

	@InjectMocks
	private BatchScheduler batchScheduler;

	@Mock
	private UserReader userReader;
	@Mock
	private PageableFactory pageableFactory;
	@Mock
	private UserInfoWriter userInfoWriter;
	@Mock
	private CeremonyWriter ceremonyWriter;
	@Mock
	private SocialAccountWriter socialAccountWriter;
	@Mock
	private AdmissionWriter userAdmissionWriter;
	@Mock
	private UserWriter userWriter;
	@Mock
	private UserProfileImageService userProfileImageService;
	@Mock
	private LockerExpirationService lockerExpirationService;

	@Test
	@DisplayName("유예기간 지난 탈퇴 유저가 있으면 후처리 writer들을 순서대로 호출한다")
	void scheduleCleanupDeactivatedUsers_Success() {
		// given
		User user1 = mock(User.class);
		User user2 = mock(User.class);
		List<User> withdrawnUsers = List.of(user1, user2);

		when(pageableFactory.create(anyInt(), anyInt())).thenReturn(PageRequest.of(0, 10));

		when(
			userReader.findCleanupTargets(
				any(LocalDateTime.class),
				any(Pageable.class)))
			.thenReturn(withdrawnUsers, List.of());

		// when
		batchScheduler.scheduleCleanupDeactivatedUsers();

		// then
		verify(userReader, times(2)).findCleanupTargets(
			any(LocalDateTime.class),
			any(Pageable.class));
		verify(userProfileImageService, times(1)).cleanupProfileImagesForBatch(anyList());
		verify(userInfoWriter).deleteUserInfoByUsers(withdrawnUsers);
		verify(ceremonyWriter).deleteCeremonyByUsers(withdrawnUsers);
		verify(socialAccountWriter).deleteSocialAccountsByUsers(withdrawnUsers);
		verify(userAdmissionWriter).deleteAdmissionByUsers(withdrawnUsers);
		verify(userWriter).cleanupWithdrawnUsers(withdrawnUsers);
	}

	@Test
	@DisplayName("유예기간 지난 탈퇴 유저가 없으면 후처리 writer를 호출하지 않는다")
	void scheduleCleanupDeactivatedUsers_NoTarget() {
		// given
		when(pageableFactory.create(anyInt(), anyInt())).thenReturn(PageRequest.of(0, 10));

		when(
			userReader.findCleanupTargets(
				any(LocalDateTime.class),
				any(Pageable.class)))
			.thenReturn(List.of());

		// when
		batchScheduler.scheduleCleanupDeactivatedUsers();

		// then
		verify(userReader).findCleanupTargets(
			any(LocalDateTime.class),
			any(Pageable.class));
		verifyNoInteractions(userInfoWriter, ceremonyWriter, socialAccountWriter, userAdmissionWriter, userWriter);
	}

	@Test
	@DisplayName("방치된 GUEST 유저가 있으면 프로필·소셜·User를 순서대로 정리한다")
	void givenStaleGuestUsers_whenScheduleCleanup_thenCleansUpInOrder() {
		// given
		User guest1 = mock(User.class);
		User guest2 = mock(User.class);
		List<User> staleGuests = List.of(guest1, guest2);

		when(pageableFactory.create(anyInt(), anyInt())).thenReturn(PageRequest.of(0, 10));
		when(userReader.findUsersByStateAndUpdatedAtBefore(
			eq(UserState.GUEST), any(LocalDateTime.class), any(Pageable.class)))
			.thenReturn(new SliceImpl<>(staleGuests, PageRequest.of(0, 10), false));

		// when
		batchScheduler.scheduleCleanupStaleGuestUsers();

		// then
		verify(userReader).findUsersByStateAndUpdatedAtBefore(
			eq(UserState.GUEST), any(LocalDateTime.class), any(Pageable.class));
		verify(userProfileImageService).cleanupProfileImagesForBatch(staleGuests);
		verify(socialAccountWriter).deleteSocialAccountsByUsers(staleGuests);
		verify(userWriter).hardDeleteUsers(staleGuests);
	}

	@Test
	@DisplayName("방치된 GUEST 유저가 없으면 정리 로직을 호출하지 않는다")
	void givenNoStaleGuestUsers_whenScheduleCleanup_thenDoesNothing() {
		// given
		when(pageableFactory.create(anyInt(), anyInt())).thenReturn(PageRequest.of(0, 10));
		when(userReader.findUsersByStateAndUpdatedAtBefore(
			eq(UserState.GUEST), any(LocalDateTime.class), any(Pageable.class)))
			.thenReturn(new SliceImpl<>(List.of(), PageRequest.of(0, 10), false));

		// when
		batchScheduler.scheduleCleanupStaleGuestUsers();

		// then
		verify(userReader).findUsersByStateAndUpdatedAtBefore(
			eq(UserState.GUEST), any(LocalDateTime.class), any(Pageable.class));
		verifyNoInteractions(userProfileImageService, socialAccountWriter, userWriter);
	}

	@Test
	@DisplayName("배치 시스템 계정으로 만료 사물함 일괄 반납을 위임한다")
	void givenSystemUser_whenScheduleReleaseExpiredLockers_thenDelegatesToService() {
		// given
		User systemUser = mock(User.class);

		when(userReader.findByEmail(StaticValue.SYSTEM_BATCH_ACCOUNT)).thenReturn(Optional.of(systemUser));
		when(lockerExpirationService.releaseExpiredLockers(systemUser))
			.thenReturn(new LockerExpirationResult(2, 1, 0));

		// when
		batchScheduler.scheduleReleaseExpiredLockers();

		// then
		verify(userReader).findByEmail(StaticValue.SYSTEM_BATCH_ACCOUNT);
		verify(lockerExpirationService).releaseExpiredLockers(systemUser);
	}

	@Test
	@DisplayName("배치 시스템 계정이 없으면 배치 실패 예외를 던진다")
	void givenNoSystemUser_whenScheduleReleaseExpiredLockers_thenThrowsException() {
		// given
		when(userReader.findByEmail(StaticValue.SYSTEM_BATCH_ACCOUNT)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> batchScheduler.scheduleReleaseExpiredLockers())
			.isInstanceOf(InternalServerException.class);
		verifyNoInteractions(lockerExpirationService);
	}

	@Test
	@DisplayName("만료 사물함 배치는 매일 03:30 크론으로 등록된다")
	void scheduleReleaseExpiredLockers_isRegisteredWithCron() throws NoSuchMethodException {
		// when
		Scheduled scheduled = BatchScheduler.class
			.getMethod("scheduleReleaseExpiredLockers")
			.getAnnotation(Scheduled.class);

		// then
		assertThat(scheduled).isNotNull();
		assertThat(scheduled.cron()).isEqualTo("0 30 3 * * ?");
	}
}
