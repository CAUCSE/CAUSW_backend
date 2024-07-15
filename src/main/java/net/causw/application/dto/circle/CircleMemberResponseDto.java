package net.causw.application.dto.circle;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.domain.model.enums.CircleMemberStatus;

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

    public static CircleMemberResponseDto from(
            User user,
            CircleMember circleMember
    ) {
        return CircleMemberResponseDto.builder()
                .id(circleMember.getId())
                .status(circleMember.getStatus())
                .circle(CircleResponseDto.from(circleMember.getCircle()))
                .user(UserResponseDto.from(user))
                .build();
    }

}
