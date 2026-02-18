package net.causw.app.main.domain.user.account.api.v2.dto.request;

import net.causw.app.main.domain.user.account.enums.user.UserState;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 재학인증 신청 목록 조회 요청")
public record AdmissionListRequest(

	@Schema(description = "이름 또는 학번 검색 키워드", example = "홍길동") String keyword,

	@Schema(description = "유저 상태 필터링 (AWAIT: 승인 대기, REJECT: 거부)", example = "AWAIT") UserState userState) {
}
