package net.causw.domain.model.user;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
public class UserAdmissionDomainModel {
    private String id;

    private String attachImage;

    @Size(max = 255, message = "소개글은 255글자이상으로 작성할 수 없습니다.")
    private String description;

    @NotNull(message = "사용자가 입력되지 않았습니다.")
    private UserDomainModel user;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserAdmissionDomainModel(
            String id,
            UserDomainModel user,
            String attachImage,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.user = user;
        this.attachImage = attachImage;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserAdmissionDomainModel of(
            String id,
            UserDomainModel user,
            String attachImage,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new UserAdmissionDomainModel(
                id,
                user,
                attachImage,
                description,
                createdAt,
                updatedAt
        );
    }

    public static UserAdmissionDomainModel of(
            UserDomainModel user,
            String attachImage,
            String description
    ) {
        return new UserAdmissionDomainModel(
                null,
                user,
                attachImage,
                description,
                null,
                null
        );
    }
}
