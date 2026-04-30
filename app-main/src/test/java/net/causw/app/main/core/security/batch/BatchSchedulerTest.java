package net.causw.app.main.core.security.batch;

import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import net.causw.app.main.core.batch.BatchScheduler;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.domain.user.account.service.implementation.AdmissionWriter;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountWriter;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoWriter;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.shared.infra.firebase.FcmUtils;
import net.causw.app.main.shared.pageable.PageableFactory;

@ExtendWith(MockitoExtension.class)
public class BatchSchedulerTest {

	@InjectMocks
	private BatchScheduler batchScheduler;

	@Mock
	private JobLauncher jobLauncher;
	@Mock
	private FcmUtils fcmUtils;
	@Mock
	private UserRepository userRepository;
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
	private Job cleanUpUnusedFilesJob;

	@Test
	@DisplayName("유예기간 지난 탈퇴 유저가 있으면 후처리 writer들을 순서대로 호출한다")
	void scheduleCleanupDeactivatedUsers_Success() {
		// given
		User user1 = mock(User.class);
		User user2 = mock(User.class);
		List<User> withdrawnUsers = List.of(user1, user2);

		when(pageableFactory.create(anyInt(), anyInt())).thenReturn(PageRequest.of(0, 10));

		when(
			userRepository.findAllByDeletedAtIsNotNullAndDeletedAtBefore(any(LocalDateTime.class), any(Pageable.class)))
			.thenReturn(new PageImpl<>(withdrawnUsers), Page.empty());

		// when
		batchScheduler.scheduleCleanupDeactivatedUsers();

		// then
		verify(userRepository).findAllByDeletedAtIsNotNullAndDeletedAtBefore(
			any(LocalDateTime.class),
			any(Pageable.class));
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
			userRepository.findAllByDeletedAtIsNotNullAndDeletedAtBefore(any(LocalDateTime.class), any(Pageable.class)))
			.thenReturn(Page.empty());

		// when
		batchScheduler.scheduleCleanupDeactivatedUsers();

		// then
		verify(userRepository).findAllByDeletedAtIsNotNullAndDeletedAtBefore(any(LocalDateTime.class),
			any(Pageable.class));
		verifyNoInteractions(userInfoWriter, ceremonyWriter, socialAccountWriter, userAdmissionWriter, userWriter);
	}
}
