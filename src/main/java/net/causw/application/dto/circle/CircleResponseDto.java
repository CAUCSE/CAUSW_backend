package net.causw.application.dto.circle;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.circle.CircleDomainModel;
import net.causw.domain.model.user.UserDomainModel;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
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


    public static CircleResponseDto from(CircleDomainModel circle) {
        return CircleResponseDto.builder()
                .id(circle.getId())
                .name(circle.getName())
                .mainImage(circle.getMainImage())
                .description(circle.getDescription())
                .isDeleted(circle.getIsDeleted())
                .leaderId(circle.getLeader().map(UserDomainModel::getId).orElse(null))
                .leaderName(circle.getLeader().map(UserDomainModel::getName).orElse(null))
                .createdAt(circle.getCreatedAt())
                .build();
    }

    public static CircleResponseDto from(CircleDomainModel circle, Long numMember) {
        return CircleResponseDto.builder()
                .id(circle.getId())
                .name(circle.getName())
                .mainImage(circle.getMainImage())
                .description(circle.getDescription())
                .isDeleted(circle.getIsDeleted())
                .leaderId(circle.getLeader().map(UserDomainModel::getId).orElse(null))
                .leaderName(circle.getLeader().map(UserDomainModel::getName).orElse(null))
                .numMember(numMember)
                .createdAt(circle.getCreatedAt())
                .build();
    }
}
