package net.causw.app.main.domain.user.auth.api.v2.dto.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 찾기 응답")
public record EmailFindResponse(
	@Schema(description = "마스킹된 이메일", example = "abc***@cau.ac.kr") String email,
	@Schema(description = "이메일 계정 생성일", example = "2020-01-02") LocalDate createdAt,
	@Schema(description = "연동된 소셜 계정 목록") List<SocialAccountSummaryResponse> socialAccounts) {
}
