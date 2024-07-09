package net.causw.application.dto.circle;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(value = "동아리원 고유 ID", example = "동아리원의 UUID 형식 유저 고유 ID 값입니다.")
    private String id;

    @ApiModelProperty(value = "동아리원의 상태입니다.", example = "MEMBER")
    private CircleMemberStatus status;

    @ApiModelProperty(value = "CircleResponseDTO", example = "동아리 responseDTO 객체를 반환합니다.")
    private CircleResponseDto circle;

    @ApiModelProperty(value = "UserResponseDTO", example = "유저 responseDTO 객체를 반환합니다.")
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
