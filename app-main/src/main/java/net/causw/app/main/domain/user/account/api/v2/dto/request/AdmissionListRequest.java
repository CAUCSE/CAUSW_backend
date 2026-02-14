package net.causw.app.main.domain.user.account.api.v2.dto.request;

import net.causw.app.main.domain.user.account.enums.user.UserState;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "관리자 재학인증 신청 목록 조회 요청")
public record AdmissionListRequest(

	@Schema(description = "이름 또는 학번 검색 키워드", example = "홍길동") String keyword,

	@Schema(description = "유저 상태 필터링 (AWAIT: 승인 대기, REJECT: 거부)", example = "AWAIT") UserState userState,

	@Schema(description = "페이지 번호 (0부터 시작)", example = "0", minimum = "0") @Min(0) Integer page,

	@Schema(description = "페이지 크기", example = "10", minimum = "1", maximum = "100") @Min(1) @Max(100) Integer size) {
}
