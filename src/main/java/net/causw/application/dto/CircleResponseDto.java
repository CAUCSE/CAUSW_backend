package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.UserDomainModel;

@Getter
@NoArgsConstructor
public class CircleResponseDto {
    private String id;
    private String name;
    private String mainImage;
    private String description;
    private Boolean isDeleted;
    private String leaderId;
    private String leaderName;
    private Long numMember;

    private CircleResponseDto(
            String id,
            String name,
            String mainImage,
            String description,
            Boolean isDeleted,
            String leaderId,
            String leaderName
    ) {
        this.id = id;
        this.name = name;
        this.mainImage = mainImage;
        this.description = description;
        this.isDeleted = isDeleted;
        this.leaderId = leaderId;
        this.leaderName = leaderName;
    }

    private CircleResponseDto(
            String id,
            String name,
            String mainImage,
            String description,
            Boolean isDeleted,
            String leaderId,
            String leaderName,
            Long numMember
    ) {
        this.id = id;
        this.name = name;
        this.mainImage = mainImage;
        this.description = description;
        this.isDeleted = isDeleted;
        this.leaderId = leaderId;
        this.leaderName = leaderName;
        this.numMember = numMember;
    }

    public static CircleResponseDto from(CircleDomainModel circle) {
        return new CircleResponseDto(
                circle.getId(),
                circle.getName(),
                circle.getMainImage(),
                circle.getDescription(),
                circle.getIsDeleted(),
                circle.getLeader().map(UserDomainModel::getId).orElse(null),
                circle.getLeader().map(UserDomainModel::getName).orElse(null)
        );
    }

    public static CircleResponseDto from(CircleDomainModel circle, Long numMember) {
        return new CircleResponseDto(
                circle.getId(),
                circle.getName(),
                circle.getMainImage(),
                circle.getDescription(),
                circle.getIsDeleted(),
                circle.getLeader().map(UserDomainModel::getId).orElse(null),
                circle.getLeader().map(UserDomainModel::getName).orElse(null),
                numMember
        );
    }
}
