package net.causw.app.main.domain.user.account.api.v2.dto.response;

import net.causw.app.main.domain.user.account.enums.user.UserState;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "재학정보 인증 신청 상태 응답 V2")
public record AdmissionStateResponse(

	@Schema(description = "사용자 상태 (AWAIT: 승인 대기, ACTIVE: 승인, REJECT: 거부)") UserState userState,

	@Schema(description = "재학정보 제출 여부") boolean hasAdmission,

	@Schema(description = "반려 사유 (REJECT 상태일 때만 존재)", nullable = true) String rejectReason) {
}
