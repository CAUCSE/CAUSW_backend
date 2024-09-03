package net.causw.domain.model.circle;

import lombok.Builder;
import lombok.Getter;
import net.causw.domain.model.user.UserDomainModel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Builder
public class CircleDomainModel {
    private String id;

    private String description;

    private String mainImage;

    @NotBlank(message = "소모임 이름이 입력되지 않았습니다.")
    private String name;

    @NotNull(message = "소모임 상태가 입력되지 않았습니다.")
    private Boolean isDeleted;

    @NotNull(message = "소모임장이 입력되지 않았습니다.")
    private UserDomainModel leader;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer circleTax;
    private Integer recruitMembers;

    public static CircleDomainModel of(
            String id,
            String name,
            String mainImage,
            String description,
            Boolean isDeleted,
            UserDomainModel leader,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Integer circleTax,
            Integer recruitMembers
    ) {
        return CircleDomainModel.builder()
                .id(id)
                .name(name)
                .mainImage(mainImage)
                .description(description)
                .isDeleted(isDeleted)
                .leader(leader)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .circleTax(circleTax)
                .recruitMembers(recruitMembers)
                .build();
    }

    public static CircleDomainModel of(
            String name,
            String mainImage,
            String description,
            UserDomainModel leader
    ) {
        return CircleDomainModel.builder()
                .name(name)
                .mainImage(mainImage)
                .description(description)
                .leader(leader)
                .build();
    }

    public void update(
            String name,
            String mainImage,
            String description,
            Integer circleTax,
            Integer recruitMembers
    ) {
        this.name = name;
        this.mainImage = mainImage;
        this.description = description;
        this.circleTax = circleTax;
        this.recruitMembers = recruitMembers;
    }

    public Optional<UserDomainModel> getLeader() {
        return Optional.ofNullable(this.leader);
    }
}
