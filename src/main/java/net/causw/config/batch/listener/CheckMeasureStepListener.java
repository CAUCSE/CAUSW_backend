package net.causw.config.batch.listener;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class CheckMeasureStepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        // Step 실행 전 로직: Step 시작 시간 기록
        stepExecution.getExecutionContext().putLong("startTime", System.currentTimeMillis());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        // Step 실행 후 로직

        // 마지막 pageNum 가져오기
        int dataRow = stepExecution.getJobExecution().getExecutionContext().getInt("dataRow", 0);

        // 실행 시간 계산
        long startTime = stepExecution.getExecutionContext().getLong("startTime");
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // 실행 시간과 마지막 pageNum을 ExitStatus에 기록
        String exitDescription = "Completed Step. Last PageNum: " + dataRow + ", Execution Time: " + executionTime + " ms";
        stepExecution.setExitStatus(new ExitStatus("COMPLETED", exitDescription));

        return ExitStatus.COMPLETED;
    }

}
