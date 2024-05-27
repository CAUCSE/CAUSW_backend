package net.causw.domain.model.circle;

import lombok.Builder;
import lombok.Getter;
import net.causw.domain.model.enums.CircleMemberStatus;

import java.time.LocalDateTime;

@Getter
@Builder
public class CircleMemberDomainModel {
    private String id;

    private CircleMemberStatus status;

    private CircleDomainModel circle;

    private String userId;

    private String userName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static CircleMemberDomainModel of(
            String id,
            CircleMemberStatus status,
            CircleDomainModel circle,
            String userId,
            String userName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return CircleMemberDomainModel.builder()
                .id(id)
                .status(status)
                .circle(circle)
                .userId(userId)
                .userName(userName)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
