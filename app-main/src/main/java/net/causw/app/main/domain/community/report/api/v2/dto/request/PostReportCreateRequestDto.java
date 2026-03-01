package net.causw.app.main.domain.community.report.api.v2.dto.request;

import net.causw.app.main.domain.community.report.enums.ReportReason;

import jakarta.validation.constraints.NotNull;

public record PostReportCreateRequestDto(
	@NotNull ReportReason reportReason) {
}
