package net.causw.app.main.domain.admin.audit.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "감사 로그 대상 정보")
public record AuditTargetResponse(

	@Schema(description = "대상 타입", example = "USER") String type,

	@Schema(description = "대상 ID", example = "11112222-aaaa-3333-bbbb-446655440000") String id,

	@Schema(description = "로그 발생 시점의 대상 이메일", example = "user@causw.net") String email,

	@Schema(description = "로그 발생 시점의 대상 이름", example = "홍길동") String name,

	@Schema(description = "로그 발생 시점의 대상 학번", example = "20201234") String studentId) {
}
