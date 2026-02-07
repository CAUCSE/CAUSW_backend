package net.causw.app.main.domain.user.academic.api.v2.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AcademicReturnApplicationRejectRequest(
        @NotBlank(message = "반려 사유는 필수입니다.")
        String reason
) {
}
