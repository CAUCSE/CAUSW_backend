package net.causw.application.dto.circle;

import lombok.Getter;
import lombok.Setter;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.domain.model.circle.CircleMemberDomainModel;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.user.UserDomainModel;

@Getter
@Setter
public class CircleMemberResponseDto {
    private String id;
    private CircleMemberStatus status;
    private CircleResponseDto circle;
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
