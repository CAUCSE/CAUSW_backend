package net.causw.application.dto.circle;

import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.UserDomainModel;

import java.time.LocalDateTime;

@Getter
@Setter
public class CircleResponseDto {
    private String id;
    private String name;
    private String mainImage;
    private String description;
    private Boolean isDeleted;
    private String leaderId;
    private String leaderName;
    private Long numMember;
    private LocalDateTime createdAt;

    private CircleResponseDto(
            String id,
            String name,
            String mainImage,
            String description,
            Boolean isDeleted,
            String leaderId,
            String leaderName,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.name = name;
        this.mainImage = mainImage;
        this.description = description;
        this.isDeleted = isDeleted;
        this.leaderId = leaderId;
        this.leaderName = leaderName;
        this.createdAt = createdAt;
    }

    private CircleResponseDto(
            String id,
            String name,
            String mainImage,
            String description,
            Boolean isDeleted,
            String leaderId,
            String leaderName,
            Long numMember,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.name = name;
        this.mainImage = mainImage;
        this.description = description;
        this.isDeleted = isDeleted;
        this.leaderId = leaderId;
        this.leaderName = leaderName;
        this.numMember = numMember;
        this.createdAt = createdAt;
    }

    public static CircleResponseDto from(CircleDomainModel circle) {
        return new CircleResponseDto(
                circle.getId(),
                circle.getName(),
                circle.getMainImage(),
                circle.getDescription(),
                circle.getIsDeleted(),
                circle.getLeader().map(UserDomainModel::getId).orElse(null),
                circle.getLeader().map(UserDomainModel::getName).orElse(null),
                circle.getCreatedAt()
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
                numMember,
                circle.getCreatedAt()
        );
    }
}
