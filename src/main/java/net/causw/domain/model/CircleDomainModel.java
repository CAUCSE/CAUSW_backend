package net.causw.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.swing.text.html.Option;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
public class CircleDomainModel {
    private String id;
    private String mainImage;
    private String description;

    @NotBlank(message = "Name is blank")
    private String name;

    @NotNull(message = "Circle state is null")
    private Boolean isDeleted;

    @NotNull(message = "Circle leader is null")
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

    public Optional<UserDomainModel> getLeader() {
        return Optional.of(this.leader);
    }
}
