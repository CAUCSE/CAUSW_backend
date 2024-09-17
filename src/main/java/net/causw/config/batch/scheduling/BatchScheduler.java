package net.causw.config.batch.scheduling;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;

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

}