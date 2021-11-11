package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.UserAdmissionDomainModel;
import net.causw.domain.model.UserDomainModel;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class UserAdmissionResponseDto {
    private String id;
    private UserResponseDto user;
    private String attachImage;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserAdmissionResponseDto(
            String id,
            UserResponseDto user,
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

    public static UserAdmissionResponseDto from(UserAdmissionDomainModel userAdmissionDomainModel) {
        return new UserAdmissionResponseDto(
                userAdmissionDomainModel.getId(),
                UserResponseDto.from(userAdmissionDomainModel.getUser()),
                userAdmissionDomainModel.getAttachImage(),
                userAdmissionDomainModel.getDescription(),
                userAdmissionDomainModel.getCreatedAt(),
                userAdmissionDomainModel.getUpdatedAt()
        );
    }

    public static UserAdmissionResponseDto from(
            UserAdmissionDomainModel userAdmissionDomainModel,
            UserDomainModel user
    ) {
        return new UserAdmissionResponseDto(
                userAdmissionDomainModel.getId(),
                UserResponseDto.from(user),
                userAdmissionDomainModel.getAttachImage(),
                userAdmissionDomainModel.getDescription(),
                userAdmissionDomainModel.getCreatedAt(),
                userAdmissionDomainModel.getUpdatedAt()
        );
    }
}
