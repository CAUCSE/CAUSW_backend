package net.causw.app.main.domain.user.academic.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "학적 변경 신청 반려 요청")
public record AcademicRecordApplicationRejectRequest(
        @Schema(description = "반려 사유", example = "제출 서류가 불충분합니다.")
        @NotBlank(message = "반려 사유는 필수입니다.")
        String rejectReason
) {
}
