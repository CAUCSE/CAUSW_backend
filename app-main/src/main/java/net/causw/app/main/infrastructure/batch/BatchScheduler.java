package net.causw.app.main.infrastructure.batch;

import java.time.LocalDateTime;
import java.time.temporal.IsoFields;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;

import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.infrastructure.firebase.FcmUtils;
import net.causw.app.main.repository.user.UserRepository;
import net.causw.app.main.service.pageable.PageableFactory;
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
	private final FcmUtils fcmUtils;
	private final UserRepository userRepository;
	private final PageableFactory pageableFactory;

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
			log.error("Batch job failed: {}", e.getMessage());  // 예외 로깅 추가
			throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.BATCH_FAIL + e.getMessage());
		}
	}

	@Scheduled(cron = "0 0 5 ? * MON")
	public void scheduleCleanInvalidFcmTokens() {
		if (!isEvenWeek())
			return;

		try {
			log.info("[FCM 배치] 유효하지 않은 FCM 토큰 정리 시작");

			int pageNum = 0;
			Page<User> userPage;
			do {
				userPage = userRepository.findAll(pageableFactory.create(pageNum++, StaticValue.BATCH_USER_LIST_SIZE));
				userPage.forEach(fcmUtils::cleanInvalidFcmTokens);
			} while (!userPage.isLast());

			log.info("[FCM 배치] 유효하지 않은 FCM 토큰 정리 완료");
		} catch (Exception e) {
			log.error("FCM 정리 배치 실패: {}", e.getMessage(), e);
			throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.BATCH_FAIL + e.getMessage());
		}
	}

	private boolean isEvenWeek() {
		return LocalDateTime.now().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) % 2 == 0;
	}

}