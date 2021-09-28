package net.causw.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CircleMemberDomainModel {
    private String id;
    private CircleMemberStatus status;
    private CircleDomainModel circle;
    private String userId;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CircleMemberDomainModel(
            String id,
            CircleMemberStatus status,
            CircleDomainModel circle,
            String userId,
            String userName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.status = status;
        this.circle = circle;
        this.userId = userId;
        this.userName = userName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CircleMemberDomainModel of(
            String id,
            CircleMemberStatus status,
            CircleDomainModel circle,
            String userId,
            String userName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new CircleMemberDomainModel(
                id,
                status,
                circle,
                userId,
                userName,
                createdAt,
                updatedAt
        );
    }

}
