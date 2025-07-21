package net.causw.app.main.infrastructure.batch.listener;

import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class DeleteUnusedFileJobCompletionNotificationListener implements JobExecutionListener {

    @Override
    public void beforeJob(@NotNull JobExecution jobExecution) {
        // Step 실행 전 로직: Step 시작 시간 기록
        jobExecution.getExecutionContext().putLong("startTime", System.currentTimeMillis());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus().isUnsuccessful()) {
            jobExecution.setExitStatus(ExitStatus.FAILED);
        } else {

            // 실행 시간 계산
            long startTime = jobExecution.getExecutionContext().getLong("startTime");
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            // 성공 시 exit_message 설정
            String exitDescription = "Deleted unused files. Execution Time: " + executionTime + " ms";
            jobExecution.setExitStatus(new ExitStatus("COMPLETED", exitDescription));
        }
    }
}