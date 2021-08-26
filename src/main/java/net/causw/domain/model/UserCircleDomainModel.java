package net.causw.domain.model;

import lombok.Getter;

@Getter
public class UserCircleDomainModel {
    private String id;
    private UserCircleStatus status;
    private CircleDomainModel circle;
    private UserDomainModel user;

    private UserCircleDomainModel(
            String id,
            UserCircleStatus status,
            CircleDomainModel circle,
            UserDomainModel user
    ) {
        this.id = id;
        this.status = status;
        this.circle = circle;
        this.user = user;
    }

    public UserCircleDomainModel of(
            String id,
            UserCircleStatus status,
            CircleDomainModel circle,
            UserDomainModel user
    ) {
        return new UserCircleDomainModel(
                id,
                status,
                circle,
                user
        );
    }

}
