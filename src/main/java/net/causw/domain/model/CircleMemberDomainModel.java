package net.causw.domain.model;

import lombok.Getter;

@Getter
public class CircleMemberDomainModel {
    private String id;
    private CircleMemberStatus status;
    private CircleDomainModel circle;
    private String userId;
    private String userName;

    private CircleMemberDomainModel(
            String id,
            CircleMemberStatus status,
            CircleDomainModel circle,
            String userId,
            String userName
    ) {
        this.id = id;
        this.status = status;
        this.circle = circle;
        this.userId = userId;
        this.userName = userName;
    }

    public static CircleMemberDomainModel of(
            String id,
            CircleMemberStatus status,
            CircleDomainModel circle,
            String userId,
            String userName
    ) {
        return new CircleMemberDomainModel(
                id,
                status,
                circle,
                userId,
                userName
        );
    }

}
