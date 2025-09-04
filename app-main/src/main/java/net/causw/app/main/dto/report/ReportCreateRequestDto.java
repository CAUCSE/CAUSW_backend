package net.causw.app.main.dto.report;

import net.causw.app.main.domain.model.enums.report.ReportReason;
import net.causw.app.main.domain.model.enums.report.ReportType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportCreateRequestDto {

	@Schema(description = "신고 대상 타입(POST | COMMENT | CHILD_COMMENT)", example = "POST")
	@NotNull(message = "신고 대상 타입은 필수입니다.")
	private ReportType reportType;

	@Schema(description = "신고 대상 컨텐츠 ID", example = "550e8400-e29b-41d4-a716-446655440000")
	@NotNull(message = "신고 대상 컨텐츠 ID는 필수입니다.")
	private String targetId;

	@Schema(description = "신고 사유(SPAM_AD | ABUSE_LANGUAGE | COMMERCIAL_AD | INAPPROPRIATE_CONTENT | FRAUD_IMPERSONATION | OFF_TOPIC | POLITICAL_CONTENT | ILLEGAL_VIDEO)", example = "SPAM_AD")
	@NotNull(message = "신고 사유는 필수입니다.")
	private ReportReason reportReason;
}