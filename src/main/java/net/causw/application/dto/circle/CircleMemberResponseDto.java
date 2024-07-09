package net.causw.application.dto.circle;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.domain.model.circle.CircleMemberDomainModel;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.user.UserDomainModel;

@Getter
@Setter
public class CircleMemberResponseDto {

    @Schema(description = "동아리원 고유 ID", example = "동아리원의 UUID 형식 유저 고유 ID 값입니다.")
    private String id;

    @Schema(description = "동아리원의 상태입니다.", example = "MEMBER")
    private CircleMemberStatus status;

    @Schema(description = "동아리 정보")
    private CircleResponseDto circle;

    @Schema(description = "유저 정보")
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
                CircleResponseDto.from(circleMember.getCircle()),
                UserResponseDto.from(user)
        );
    }
}
