package net.causw.app.main.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class ReportCreateResponseDto {
    
    @Schema(description = "응답 메시지", example = "신고가 접수되었습니다. 검토까지는 최대 24시간이 소요됩니다.")
    private final String message;
    
    public ReportCreateResponseDto(String message) {
        this.message = message;
    }
}