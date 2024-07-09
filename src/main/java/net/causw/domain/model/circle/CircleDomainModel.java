package net.causw.domain.model.circle;

import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.user.UserDomainModel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
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

    private CircleDomainModel(
            String id,
            String name,
            String mainImage,
            String description,
            Boolean isDeleted,
            UserDomainModel leader,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.mainImage = mainImage;
        this.description = description;
        this.isDeleted = isDeleted;
        this.leader = leader;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CircleDomainModel of(
            String id,
            String name,
            String mainImage,
            String description,
            Boolean isDeleted,
            UserDomainModel leader,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new CircleDomainModel(
                id,
                name,
                mainImage,
                description,
                isDeleted,
                leader,
                createdAt,
                updatedAt
        );
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
                leader,
                null,
                null
        );
    }

    public static CircleDomainModel of(
            String name,
            String mainImage,
            String description,
            UserDomainModel leader
    ) {
        return new CircleDomainModel(
                null,
                name,
                mainImage,
                description,
                false,
                leader,
                null,
                null
        );
    }

    public void update(
            String name,
            String mainImage,
            String description
    ) {
        this.name = name;
        this.mainImage = mainImage;
        this.description = description;
    }

    public Optional<UserDomainModel> getLeader() {
        return Optional.ofNullable(this.leader);
    }
}
