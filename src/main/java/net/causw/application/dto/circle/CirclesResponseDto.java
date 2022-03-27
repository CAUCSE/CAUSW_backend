package net.causw.application.dto.circle;

import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.UserDomainModel;

import java.time.LocalDateTime;

@Getter
@Setter
public class CirclesResponseDto {
    private String id;
    private String name;
    private String mainImage;
    private String description;
    private String leaderId;
    private String leaderName;
    private Long numMember;
    private Boolean isJoined;

    private LocalDateTime createdAt;
    private LocalDateTime joinedAt;

    private CirclesResponseDto(
            String id,
            String name,
            String mainImage,
            String description,
            String leaderId,
            String leaderName,
            Long numMember,
            Boolean isJoined,
            LocalDateTime createdAt,
            LocalDateTime joinedAt
    ) {
        this.id = id;
        this.name = name;
        this.mainImage = mainImage;
        this.description = description;
        this.leaderId = leaderId;
        this.leaderName = leaderName;
        this.numMember = numMember;
        this.isJoined = isJoined;
        this.createdAt = createdAt;
        this.joinedAt = joinedAt;
    }

    public static CirclesResponseDto from(
            CircleDomainModel circleDomainModel,
            Long numMember
    ) {
        return new CirclesResponseDto(
                circleDomainModel.getId(),
                circleDomainModel.getName(),
                circleDomainModel.getMainImage(),
                circleDomainModel.getDescription(),
                circleDomainModel.getLeader().map(UserDomainModel::getId).orElse(null),
                circleDomainModel.getLeader().map(UserDomainModel::getName).orElse(null),
                numMember,
                false,
                circleDomainModel.getCreatedAt(),
                null
        );
    }

    public static CirclesResponseDto from(
            CircleDomainModel circleDomainModel,
            Long numMember,
            LocalDateTime joinedAt
    ) {
        return new CirclesResponseDto(
                circleDomainModel.getId(),
                circleDomainModel.getName(),
                circleDomainModel.getMainImage(),
                circleDomainModel.getDescription(),
                circleDomainModel.getLeader().map(UserDomainModel::getId).orElse(null),
                circleDomainModel.getLeader().map(UserDomainModel::getName).orElse(null),
                numMember,
                true,
                circleDomainModel.getCreatedAt(),
                joinedAt
        );
    }
}
