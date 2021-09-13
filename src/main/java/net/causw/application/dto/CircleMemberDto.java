package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.CircleMember;
import net.causw.domain.model.CircleMemberStatus;

@Getter
@NoArgsConstructor
public class CircleMemberDto {
    private String id;
    private CircleMemberStatus status;
    private CircleFullDto circle;
    private UserResponseDto user;

    private CircleMemberDto(
            String id,
            CircleMemberStatus status,
            CircleFullDto circle,
            UserResponseDto user
    ) {
        this.id = id;
        this.status = status;
        this.circle = circle;
        this.user = user;
    }

    public static CircleMemberDto from(CircleMember circleMember) {
        return new CircleMemberDto(
                circleMember.getId(),
                circleMember.getStatus(),
                CircleFullDto.from(
                        circleMember.getCircle()
                ),
                UserResponseDto.from(
                        circleMember.getUser()
                )
        );
    }
}
