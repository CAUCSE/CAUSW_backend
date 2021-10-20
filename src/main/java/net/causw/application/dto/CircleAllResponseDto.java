package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.CircleDomainModel;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CircleAllResponseDto {
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

    private CircleAllResponseDto(
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

    public static CircleAllResponseDto from(CircleDomainModel circleDomainModel, Long numMember) {
        return new CircleAllResponseDto(
                circleDomainModel.getId(),
                circleDomainModel.getName(),
                circleDomainModel.getMainImage(),
                circleDomainModel.getDescription(),
                circleDomainModel.getLeader().getId(),
                circleDomainModel.getLeader().getName(),
                numMember,
                false,
                circleDomainModel.getCreatedAt(),
                null
        );
    }

    public static CircleAllResponseDto from(
            CircleDomainModel circleDomainModel,
            Long numMember,
            LocalDateTime joinedAt
    ) {
        return new CircleAllResponseDto(
                circleDomainModel.getId(),
                circleDomainModel.getName(),
                circleDomainModel.getMainImage(),
                circleDomainModel.getDescription(),
                circleDomainModel.getLeader().getId(),
                circleDomainModel.getLeader().getName(),
                numMember,
                true,
                circleDomainModel.getCreatedAt(),
                joinedAt
        );
    }
}
