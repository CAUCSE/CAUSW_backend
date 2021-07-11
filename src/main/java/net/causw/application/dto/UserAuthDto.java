package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.UserDomainModel;
import net.causw.infra.UserAuth;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class UserAuthDto {
    private String id;
    private String image;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDetailDto user;

    private UserAuthDto(
            String id,
            String image,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            UserDetailDto user
    ) {
        this.id = id;
        this.image = image;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.user = user;
    }

    public static UserAuthDto of(UserAuth userAuth) {
        return new UserAuthDto(
                userAuth.getId(),
                userAuth.getImage(),
                userAuth.getCreatedAt(),
                userAuth.getUpdatedAt(),
                UserDetailDto.of(
                        UserDomainModel.of(userAuth.getUser())
                )
        );
    }
}
