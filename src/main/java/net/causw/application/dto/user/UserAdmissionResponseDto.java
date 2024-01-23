package net.causw.application.dto.user;

import io.swagger.annotations.*;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.user.UserAdmissionDomainModel;
import net.causw.domain.model.user.UserDomainModel;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserAdmissionResponseDto {

    @ApiModelProperty(value = "admission id", example = "ff7576321d26d71c018d26dc284a9999", required = true)
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
