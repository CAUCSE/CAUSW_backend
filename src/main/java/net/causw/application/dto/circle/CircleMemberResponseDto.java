package net.causw.application.dto.circle;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.domain.model.circle.CircleMemberDomainModel;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.user.UserDomainModel;

@Getter
@Setter
public class CircleMemberResponseDto {

    @ApiModelProperty(value = "동아리원 고유 ID", example = "동아리원의 UUID 형식 유저 고유 ID 값입니다.")
    private String id;

    @ApiModelProperty(value = "동아리원의 상태입니다.", example = "MEMBER")
    private CircleMemberStatus status;

    @ApiModelProperty(value = "CircleResponseDTO", example = "동아리 responseDTO 객체를 반환합니다.")
    private CircleResponseDto circle;

    @ApiModelProperty(value = "UserResponseDTO", example = "유저 responseDTO 객체를 반환합니다.")
    private UserResponseDto user;

    private CircleMemberResponseDto(
            String id,
            CircleMemberStatus status,
            CircleResponseDto circle,
            UserResponseDto user
    ) {
        this.id = id;
        this.status = status;
        this.circle = circle;
        this.user = user;
    }

    public static CircleMemberResponseDto from(
            UserDomainModel user,
            CircleMemberDomainModel circleMember
    ) {
        return new CircleMemberResponseDto(
                circleMember.getId(),
                circleMember.getStatus(),
                CircleResponseDto.from(
                        circleMember.getCircle()
                ),
                UserResponseDto.from(user)
        );
    }
}
