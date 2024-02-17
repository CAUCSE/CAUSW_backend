package net.causw.application.dto.circle;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.circle.CircleDomainModel;
import net.causw.domain.model.user.UserDomainModel;

import java.time.LocalDateTime;

@Getter
@Setter
public class CirclesResponseDto {

    @ApiModelProperty(value = "동아리 ID", example = "UUID 형식의 동아리 고유 ID String 값입니다.")
    private String id;

    @ApiModelProperty(value = "동아리 이름", example = "소프트웨어학부 특별기구 ICT위원회 동문 네트워크")
    private String name;

    @ApiModelProperty(value = "동아리 메인 이미지(nullable)", example = "String")
    private String mainImage;

    @ApiModelProperty(value = "동아리 설명", example = "ICT위원회는 동문 네트워크 서비스를 만드는 특별기구이자 동아리입니다.")
    private String description;

    @ApiModelProperty(value = "동아리장 ID", example = "UUID 형식의 동아리장 ID(PK) String 값입니다.")
    private String leaderId;

    @ApiModelProperty(value = "동아리장 이름", example = "정상제")
    private String leaderName;

    @ApiModelProperty(value = "동아리장 숫자", example = "7")
    private Long numMember;

    @ApiModelProperty(value = "유저의 동아리 가입 여부\n(User Role ADMIN 일 시 항상 true)", example = "false")
    private Boolean isJoined;

    @ApiModelProperty(value = "동아리 생성 일시", example = "2024-02-04 16:11:02.342644")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "동아리 가입 시점\n(User Role ADMIN 일 시 항상 API 호출 시점)", example = "2024-02-04 16:11:02.342644")
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
