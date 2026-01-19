package net.causw.app.main.domain.campus.circle.api.v1.dto;

import net.causw.app.main.domain.user.account.api.v1.dto.UserResponseDto;
import net.causw.app.main.domain.campus.circle.enums.CircleMemberStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CircleMemberResponseDto {

	@Schema(description = "동아리원 고유 ID", example = "동아리원의 UUID 형식 유저 고유 ID 값입니다.")
	private String id;

	@Schema(description = "동아리원의 상태입니다.", example = "MEMBER")
	private CircleMemberStatus status;

	@Schema(description = "동아리 정보")
	private CircleResponseDto circle;

	@Schema(description = "유저 정보")
	private UserResponseDto user;

}
