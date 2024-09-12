package net.causw.application.dto.circle;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.util.CircleServiceDtoMapper;
import net.causw.domain.model.circle.CircleDomainModel;
import net.causw.domain.model.user.UserDomainModel;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CirclesResponseDto {
    @Schema(description = "동아리 ID", example = "UUID 형식의 동아리 고유 ID String 값입니다.")
    private String id;

    @Schema(description = "동아리 이름", example = "소프트웨어학부 특별기구 ICT위원회 동문 네트워크")
    private String name;

    @Schema(description = "동아리 메인 이미지(nullable)", example = "String")
    private String mainImage;

    @Schema(description = "동아리 설명", example = "ICT위원회는 동문 네트워크 서비스를 만드는 특별기구이자 동아리입니다.")
    private String description;

    @Schema(description = "동아리장 ID", example = "UUID 형식의 동아리장 ID(PK) String 값입니다.")
    private String leaderId;

    @Schema(description = "동아리장 이름", example = "정상제")
    private String leaderName;

    @Schema(description = "동아리원 숫자", example = "7")
    private Long numMember;

    @Schema(description = "동아리 삭제 여부", example = "false")
    private Boolean isDeleted;

    @Schema(description = "유저의 동아리 가입 여부\n(User Role ADMIN 일 시 항상 true)", example = "false")
    private Boolean isJoined;

    @Schema(description = "동아리 생성 일시", example = "2024-02-04T16:11:02.342644")
    private LocalDateTime createdAt;

    @Schema(description = "동아리 가입 시점\n(User Role ADMIN 일 시 항상 API 호출 시점)", example = "2024-02-04T16:11:02.342644")
    private LocalDateTime joinedAt;

    public static CirclesResponseDto from(
            Circle circle,
            Long numMember
    ) {
        return CirclesResponseDto.builder()
                .id(circle.getId())
                .name(circle.getName())
                .mainImage(circle.getMainImage())
                .description(circle.getDescription())
                .leaderId(circle.getLeader().map(User::getId).orElse(null))
                .leaderName(circle.getLeader().map(User::getName).orElse(null))
                .numMember(numMember)
                .isJoined(false)
                .isDeleted(circle.getIsDeleted())
                .createdAt(circle.getCreatedAt())
                .build();
    }

    public static CirclesResponseDto from(
            Circle circle,
            Long numMember,
            LocalDateTime joinedAt
    ) {
        return CirclesResponseDto.builder()
                .id(circle.getId())
                .name(circle.getName())
                .mainImage(circle.getMainImage())
                .description(circle.getDescription())
                .leaderId(circle.getLeader().map(User::getId).orElse(null))
                .leaderName(circle.getLeader().map(User::getName).orElse(null))
                .numMember(numMember)
                .isJoined(true)
                .isDeleted(circle.getIsDeleted())
                .createdAt(circle.getCreatedAt())
                .joinedAt(joinedAt)
                .build();
    }

}
