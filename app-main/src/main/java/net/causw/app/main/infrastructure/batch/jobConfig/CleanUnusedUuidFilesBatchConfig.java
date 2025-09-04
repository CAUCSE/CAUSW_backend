package net.causw.app.main.infrastructure.batch.jobConfig;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import net.causw.app.main.infrastructure.batch.listener.CheckMeasureStepListener;
import net.causw.app.main.infrastructure.batch.listener.DeleteFileStepListener;
import net.causw.app.main.infrastructure.batch.listener.DeleteUnusedFileJobCompletionNotificationListener;
import net.causw.app.main.service.uuidFile.CleanUnusedUuidFileService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CleanUnusedUuidFilesBatchConfig {

	private final CleanUnusedUuidFileService cleanUnusedUuidFileService;

	@Bean
	public RetryTemplate retryTemplate() {
		RetryTemplate retryTemplate = new RetryTemplate();

		// 최대 재시도 횟수 설정
		SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
		retryPolicy.setMaxAttempts(3); // 3번까지 재시도
		retryTemplate.setRetryPolicy(retryPolicy);

		// 재시도 간 대기 시간 설정
		FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
		backOffPolicy.setBackOffPeriod(2000L); // 2초 대기
		retryTemplate.setBackOffPolicy(backOffPolicy);

		return retryTemplate;
	}

	@Bean
	public Job cleanUpUnusedFilesJob(JobRepository jobRepository,
		DeleteUnusedFileJobCompletionNotificationListener deleteUnusedFileJobCompletionNotificationListener,
		@Qualifier("initIsUsedUuidFileIntegrationStep") Step initIsUsedUuidFileIntegrationStep,
		@Qualifier("checkIsUsedWithCalendarAttachImageIntegrationStep") Step checkIsUsedWithCalendarAttachImageIntegrationStep,
		@Qualifier("checkIsUsedWithCircleMainImageIntegrationStep") Step checkIsUsedWithCircleMainImageIntegrationStep,
		@Qualifier("checkIsUsedWithEventAttachImageIntegrationStep") Step checkIsUsedWithEventAttachImageIntegrationStep,
		@Qualifier("checkIsUsedWithPostAttachImageIntegrationStep") Step checkIsUsedWithPostAttachImageIntegrationStep,
		@Qualifier("checkIsUsedWithUserAcademicRecordApplicationAttachImageIntegrationStep") Step checkIsUsedWithUserAcademicRecordApplicationAttachImageIntegrationStep,
		@Qualifier("checkIsUsedWithUserAdmissionAttachImageIntegrationStep") Step checkIsUsedWithUserAdmissionAttachImageIntegrationStep,
		@Qualifier("checkIsUsedWithUserAdmissionLogAttachImageIntegrationStep") Step checkIsUsedWithUserAdmissionLogAttachImageIntegrationStep,
		@Qualifier("checkIsUsedWithUserProfileImageIntegrationStep") Step checkIsUsedWithUserProfileImageIntegrationStep,
		@Qualifier("deleteFileNotUsedStep") Step deleteFileNotUsedStep
	) {
		return new JobBuilder("cleanUpUnusedFilesJob", jobRepository)
			.listener(deleteUnusedFileJobCompletionNotificationListener)
			.start(initIsUsedUuidFileIntegrationStep)
			.next(checkIsUsedWithCalendarAttachImageIntegrationStep)
			.next(checkIsUsedWithCircleMainImageIntegrationStep)
			.next(checkIsUsedWithEventAttachImageIntegrationStep)
			.next(checkIsUsedWithPostAttachImageIntegrationStep)
			.next(checkIsUsedWithUserAcademicRecordApplicationAttachImageIntegrationStep)
			.next(checkIsUsedWithUserAdmissionAttachImageIntegrationStep)
			.next(checkIsUsedWithUserAdmissionLogAttachImageIntegrationStep)
			.next(checkIsUsedWithUserProfileImageIntegrationStep)
			.next(deleteFileNotUsedStep)
			.build();
	}

	@Bean
	public Step initIsUsedUuidFileIntegrationStep(JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		CheckMeasureStepListener checkMeasureStepListener) {
		return new StepBuilder("initIsUsedUuidFileIntegrationStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				cleanUnusedUuidFileService.initIsUsedUuidFileIntegration(
					chunkContext.getStepContext().getStepExecution());
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.listener(checkMeasureStepListener)
			.build();
	}

	@Bean
	public Step checkIsUsedWithCalendarAttachImageIntegrationStep(JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		CheckMeasureStepListener checkMeasureStepListener) {
		return new StepBuilder("checkIsUsedWithCalendarAttachImageIntegrationStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				cleanUnusedUuidFileService.checkIsUsedWithCalendarAttachImageIntegration(
					chunkContext.getStepContext().getStepExecution());
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.listener(checkMeasureStepListener)
			.build();
	}

	@Bean
	public Step checkIsUsedWithCircleMainImageIntegrationStep(JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		CheckMeasureStepListener checkMeasureStepListener) {
		return new StepBuilder("checkIsUsedWithCircleMainImageIntegrationStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				cleanUnusedUuidFileService.checkIsUsedWithCircleMainImageIntegration(
					chunkContext.getStepContext().getStepExecution());
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.listener(checkMeasureStepListener)
			.build();
	}

	@Bean
	public Step checkIsUsedWithEventAttachImageIntegrationStep(JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		CheckMeasureStepListener checkMeasureStepListener) {
		return new StepBuilder("checkIsUsedWithEventAttachImageIntegrationStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				cleanUnusedUuidFileService.checkIsUsedWithEventAttachImageIntegration(
					chunkContext.getStepContext().getStepExecution());
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.listener(checkMeasureStepListener)
			.build();
	}

	@Bean
	public Step checkIsUsedWithPostAttachImageIntegrationStep(JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		CheckMeasureStepListener checkMeasureStepListener) {
		return new StepBuilder("checkIsUsedWithPostAttachImageIntegrationStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				cleanUnusedUuidFileService.checkIsUsedWithPostAttachImageIntegration(
					chunkContext.getStepContext().getStepExecution());
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.listener(checkMeasureStepListener)
			.build();
	}

	@Bean
	public Step checkIsUsedWithUserAcademicRecordApplicationAttachImageIntegrationStep(JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		CheckMeasureStepListener checkMeasureStepListener) {
		return new StepBuilder("checkIsUsedWithUserAcademicRecordApplicationAttachImageIntegrationStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				cleanUnusedUuidFileService.checkIsUsedWithUserAcademicRecordApplicationAttachImageIntegration(
					chunkContext.getStepContext().getStepExecution());
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.listener(checkMeasureStepListener)
			.build();
	}

	@Bean
	public Step checkIsUsedWithUserAdmissionAttachImageIntegrationStep(JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		CheckMeasureStepListener checkMeasureStepListener) {
		return new StepBuilder("checkIsUsedWithUserAdmissionAttachImageIntegrationStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				cleanUnusedUuidFileService.checkIsUsedWithUserAdmissionAttachImageIntegration(
					chunkContext.getStepContext().getStepExecution());
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.listener(checkMeasureStepListener)
			.build();
	}

	@Bean
	public Step checkIsUsedWithUserAdmissionLogAttachImageIntegrationStep(JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		CheckMeasureStepListener checkMeasureStepListener) {
		return new StepBuilder("checkIsUsedWithUserAdmissionLogAttachImageIntegrationStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				cleanUnusedUuidFileService.checkIsUsedWithUserAdmissionLogAttachImageIntegration(
					chunkContext.getStepContext().getStepExecution());
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.listener(checkMeasureStepListener)
			.build();
	}

	@Bean
	Step checkIsUsedWithUserProfileImageIntegrationStep(JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		CheckMeasureStepListener checkMeasureStepListener) {
		return new StepBuilder("checkIsUsedWithUserProfileImageIntegrationStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				cleanUnusedUuidFileService.checkIsUsedWithUserProfileImageIntegration(
					chunkContext.getStepContext().getStepExecution());
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.listener(checkMeasureStepListener)
			.build();
	}

	@Bean
	Step deleteFileNotUsedStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
		DeleteFileStepListener deleteFileStepListener) {
		return new StepBuilder("deleteFileNotUsedStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				cleanUnusedUuidFileService.deleteFileNotUsed(chunkContext.getStepContext().getStepExecution());
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.listener(deleteFileStepListener)
			.build();
	}

}