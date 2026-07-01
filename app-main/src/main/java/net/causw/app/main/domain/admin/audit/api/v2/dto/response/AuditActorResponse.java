package net.causw.app.main.domain.admin.audit.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "감사 로그 수행자 정보")
public record AuditActorResponse(

	@Schema(description = "수행자 사용자 ID", example = "11112222-aaaa-3333-bbbb-446655440000") String userId,

	@Schema(description = "로그 발생 시점의 수행자 이메일", example = "admin@causw.net") String email,

	@Schema(description = "로그 발생 시점의 수행자 이름", example = "관리자") String name,

	@Schema(description = "로그 발생 시점의 수행자 학번", example = "20190001") String studentId) {
}
