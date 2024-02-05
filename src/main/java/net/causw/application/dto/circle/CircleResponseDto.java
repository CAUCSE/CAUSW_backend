package net.causw.application.dto.circle;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.circle.CircleDomainModel;
import net.causw.domain.model.user.UserDomainModel;

import java.time.LocalDateTime;

@Getter
@Setter
public class CircleResponseDto {

    @ApiModelProperty(value = "동아리 ID", example = "UUID 형식의 동아리 고유 ID String 값 입니다.")
    private String id;

    @ApiModelProperty(value = "동아리 이름", example = "소프트웨어학부 특별기구 ICT위원회 동문네트워크")
    private String name;

    @ApiModelProperty(value = "동아리 메인 이미지(nullable)", example = "String")
    private String mainImage;

    @ApiModelProperty(value = "동아리 설명", example = "ICT위원회는 동문 네트워크 서비스를 만드는 특별기구이자 동아리입니다.")
    private String description;

    @ApiModelProperty(value = "동아리 삭제 여부", example = "false")
    private Boolean isDeleted;

    @ApiModelProperty(value = "동아리장 ID", example = "UUID 형식의 동아리장 고유 ID 값 String 입니다.")
    private String leaderId;

    @ApiModelProperty(value = "동아리장 이름", example = "정상제")
    private String leaderName;

    @ApiModelProperty(value = "동아리원 숫자", example = "7")
    private Long numMember;

    @ApiModelProperty(value = "동아리 생성 일시", example = "2024-02-04 16:11:02.342644")
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
