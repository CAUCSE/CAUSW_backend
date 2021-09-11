package net.causw.domain.model;

import lombok.Getter;

@Getter
public class CircleMemberDomainModel {
    private String id;
    private CircleMemberStatus status;
    private CircleDomainModel circle;
    private UserDomainModel user;

    private CircleMemberDomainModel(
            String id,
            CircleMemberStatus status,
            CircleDomainModel circle,
            UserDomainModel user
    ) {
        this.id = id;
        this.status = status;
        this.circle = circle;
        this.user = user;
    }

    public CircleMemberDomainModel of(
            String id,
            CircleMemberStatus status,
            CircleDomainModel circle,
            UserDomainModel user
    ) {
        return new CircleMemberDomainModel(
                id,
                status,
                circle,
                user
        );
    }

}
