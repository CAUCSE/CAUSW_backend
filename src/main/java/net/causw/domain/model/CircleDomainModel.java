package net.causw.domain.model;

import lombok.Getter;

@Getter
public class CircleDomainModel {
    private String id;
    private String name;
    private String mainImage;
    private String description;
    private Boolean isDeleted;
    private UserDomainModel leader;

    private CircleDomainModel(
            String id,
            String name,
            String mainImage,
            String description,
            Boolean isDeleted,
            UserDomainModel leader
    ) {
        this.id = id;
        this.name = name;
        this.mainImage = mainImage;
        this.description = description;
        this.isDeleted = isDeleted;
        this.leader = leader;
    }

    public static CircleDomainModel of(
            String id,
            String name,
            String mainImage,
            String description,
            Boolean isDeleted,
            UserDomainModel leader
    ) {
        return new CircleDomainModel(
                id,
                name,
                mainImage,
                description,
                isDeleted,
                leader
        );
    }
}
