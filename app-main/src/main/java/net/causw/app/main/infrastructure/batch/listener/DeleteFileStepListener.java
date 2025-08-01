package net.causw.app.main.infrastructure.batch.listener;

import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class DeleteFileStepListener implements StepExecutionListener {

	@Override
	public void beforeStep(@NotNull StepExecution stepExecution) {
		// Step 실행 전 로직: Step 시작 시간 기록
		stepExecution.getExecutionContext().putLong("startTime", System.currentTimeMillis());
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		// 삭제된 파일 수 가져오기
		int deletedFileCount = stepExecution.getJobExecution().getExecutionContext().getInt("deletedFileCount", 0);

		// 실행 시간 계산
		long startTime = stepExecution.getExecutionContext().getLong("startTime");
		long endTime = System.currentTimeMillis();
		long executionTime = endTime - startTime;

		// Exit 메시지에 삭제된 파일 수 기록
		String exitDescription =
			"Deleted " + deletedFileCount + " unused files. Execution Time: " + executionTime + " ms";
		stepExecution.setExitStatus(new ExitStatus("COMPLETED", exitDescription));
		return stepExecution.getExitStatus();
	}
}