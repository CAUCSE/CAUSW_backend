package net.causw.application.dto.user;

import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.UserAdmissionDomainModel;
import net.causw.domain.model.UserState;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserAdmissionsResponseDto {
    private String id;
    private String userName;
    private String userEmail;
    private Integer admissionYear;
    private String attachImage;
    private String description;
    private UserState userState;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserAdmissionsResponseDto(
            String id,
            String userName,
            String userEmail,
            Integer admissionYear,
            String attachImage,
            String description,
            UserState userState,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.userName = userName;
        this.userEmail = userEmail;
        this.admissionYear = admissionYear;
        this.attachImage = attachImage;
        this.description = description;
        this.userState = userState;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserAdmissionsResponseDto from(UserAdmissionDomainModel userAdmissionDomainModel) {
        return new UserAdmissionsResponseDto(
                userAdmissionDomainModel.getId(),
                userAdmissionDomainModel.getUser().getName(),
                userAdmissionDomainModel.getUser().getEmail(),
                userAdmissionDomainModel.getUser().getAdmissionYear(),
                userAdmissionDomainModel.getAttachImage(),
                userAdmissionDomainModel.getDescription(),
                userAdmissionDomainModel.getUser().getState(),
                userAdmissionDomainModel.getCreatedAt(),
                userAdmissionDomainModel.getUpdatedAt()
        );
    }
}
