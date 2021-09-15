package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.CircleMember;
import net.causw.domain.model.CircleMemberDomainModel;
import net.causw.domain.model.CircleMemberStatus;

@Getter
@NoArgsConstructor
public class CircleMemberResponseDto {
    private String id;
    private CircleMemberStatus status;
    private CircleResponseDto circle;
    private String userId;
    private String userName;

    private CircleMemberResponseDto(
            String id,
            CircleMemberStatus status,
            CircleResponseDto circle,
            String userId,
            String userName
    ) {
        this.id = id;
        this.status = status;
        this.circle = circle;
        this.userId = userId;
        this.userName = userName;
    }

    public static CircleMemberResponseDto from(CircleMemberDomainModel circleMember) {
        return new CircleMemberResponseDto(
                circleMember.getId(),
                circleMember.getStatus(),
                CircleResponseDto.from(
                        circleMember.getCircle()
                ),
                circleMember.getUserId(),
                circleMember.getUserName()
        );
    }
}
