package net.causw.app.main.domain.user.account.api.v2.dto.response;

import net.causw.app.main.shared.dto.ProfileImageDto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserInfoSummaryResponse(
	@Schema(description = "동문 수첩 프로필 id", example = "uuid 형식의 String 값입니다") String id,

	@Schema(description = "사용자 프로필 이미지") ProfileImageDto profileImage,

	@Schema(description = "사용자 이름", example = "홍길동") String name,

	@Schema(description = "사용자 학번", example = "18학번") String admissionYear,

	@Schema(description = "사용자 학적 상태", example = "졸업생") String academicStatus,

	@Schema(description = "사용자 직업", example = "풀스택 개발자") String job,

	@Schema(description = "동문 수첩 프로필 설명", example = "동문 수첩 프로필 설명입니다.") String description) {
}
