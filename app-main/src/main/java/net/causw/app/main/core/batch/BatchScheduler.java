package net.causw.app.main.core.batch;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;

import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.domain.user.account.service.UserProfileImageService;
import net.causw.app.main.domain.user.account.service.implementation.AdmissionWriter;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountWriter;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoWriter;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.shared.pageable.PageableFactory;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.InternalServerException;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchScheduler {

	private final JobLauncher jobLauncher;
	private final UserRepository userRepository;
	private final PageableFactory pageableFactory;
	private final UserInfoWriter userInfoWriter;
	private final CeremonyWriter ceremonyWriter;
	private final SocialAccountWriter socialAccountWriter;
	private final UserWriter userWriter;
	private final AdmissionWriter admissionWriter;
	private final UserProfileImageService userProfileImageService;

	@Resource(name = "cleanUpUnusedFilesJob")
	private Job cleanUpUnusedFilesJob;

	@Scheduled(cron = "0 0 3 1 * ?") // 매달 1일 오전 3시에 실행
	public void scheduleCleanUpJob() {
		try {
			JobParameters jobParameters = new JobParametersBuilder()
				.addLong("timestamp", System.currentTimeMillis())
				.addLocalDateTime("dateTime", LocalDateTime.now())
				.toJobParameters();

			jobLauncher.run(cleanUpUnusedFilesJob, jobParameters);
		} catch (Exception e) {
			log.error("Batch job failed: {}", e.getMessage()); // 예외 로깅 추가
			throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.BATCH_FAIL + e.getMessage());
		}
	}

	@Scheduled(cron = "0 10 3 * * ?") // 매일 새벽 3시 10분
	public void scheduleCleanupDeactivatedUsers() {
		try {
			log.info("[유저 정리 배치] 비활성(탈퇴/추방) 유저 후처리 시작");

			LocalDateTime dueDate = LocalDateTime.now().minusDays(30);

			boolean hasNext;
			do {
				Page<User> userPage = userRepository.findAllByDeletedAtIsNotNullAndDeletedAtBefore(
					dueDate,
					pageableFactory.create(0, StaticValue.BATCH_USER_LIST_SIZE));

				List<User> withdrawnUsers = userPage.getContent();

				if (withdrawnUsers.isEmpty()) {
					break;
				}

				userProfileImageService.cleanupProfileImagesForBatch(withdrawnUsers);
				userInfoWriter.deleteUserInfoByUsers(withdrawnUsers);
				ceremonyWriter.deleteCeremonyByUsers(withdrawnUsers);
				socialAccountWriter.deleteSocialAccountsByUsers(withdrawnUsers);
				admissionWriter.deleteAdmissionByUsers(withdrawnUsers);
				userWriter.cleanupWithdrawnUsers(withdrawnUsers);

				hasNext = userPage.hasNext();
				log.info("[유저 정리 배치] {}명 처리 완료", withdrawnUsers.size());

			} while (hasNext);
			log.info("[유저 정리 배치] 탈퇴 유저 후처리 완료");
		} catch (Exception e) {
			log.error("유저 정리 배치 실패: {}", e.getMessage(), e);
			throw new InternalServerException(
				ErrorCode.INTERNAL_SERVER,
				MessageUtil.BATCH_FAIL + e.getMessage());
		}
	}

	@Scheduled(cron = "0 20 3 * * ?") // 매일 새벽 3시 20분 (다른 배치와 시각 충돌 방지)
	public void scheduleCleanupStaleGuestUsers() {
		try {
			log.info("[GUEST 정리 배치] 소셜로그인 대기 방치 유저 정리 시작");

			LocalDateTime dueDate = LocalDateTime.now().minusDays(7);

			boolean hasNext;
			do {
				Page<User> userPage = userRepository.findAllByStateAndUpdatedAtBefore(
					UserState.GUEST,
					dueDate,
					pageableFactory.create(0, StaticValue.BATCH_USER_LIST_SIZE));

				List<User> staleGuests = userPage.getContent();

				if (staleGuests.isEmpty()) {
					break;
				}

				// FK 참조 데이터를 먼저 정리한 뒤 User를 하드 삭제한다. (FcmToken은 cascade로 함께 삭제됨)
				userProfileImageService.cleanupProfileImagesForBatch(staleGuests);
				socialAccountWriter.deleteSocialAccountsByUsers(staleGuests);
				userWriter.deleteGuestUsers(staleGuests);

				hasNext = userPage.hasNext();
				log.info("[GUEST 정리 배치] {}명 삭제 완료", staleGuests.size());

			} while (hasNext);
			log.info("[GUEST 정리 배치] 소셜로그인 대기 방치 유저 정리 완료");
		} catch (Exception e) {
			log.error("GUEST 정리 배치 실패: {}", e.getMessage(), e);
			throw new InternalServerException(
				ErrorCode.INTERNAL_SERVER,
				MessageUtil.BATCH_FAIL + e.getMessage());
		}
	}

}