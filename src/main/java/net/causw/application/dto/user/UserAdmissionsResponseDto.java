package net.causw.application.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.user.UserAdmissionDomainModel;
import net.causw.domain.model.enums.UserState;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
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

    public static UserAdmissionsResponseDto from(UserAdmissionDomainModel userAdmissionDomainModel) {
        return UserAdmissionsResponseDto.builder()
                .id(userAdmissionDomainModel.getId())
                .userName(userAdmissionDomainModel.getUser().getName())
                .userEmail(userAdmissionDomainModel.getUser().getEmail())
                .admissionYear(userAdmissionDomainModel.getUser().getAdmissionYear())
                .attachImage(userAdmissionDomainModel.getAttachImage())
                .description(userAdmissionDomainModel.getDescription())
                .userState(userAdmissionDomainModel.getUser().getState())
                .createdAt(userAdmissionDomainModel.getCreatedAt())
                .updatedAt(userAdmissionDomainModel.getUpdatedAt())
                .build();
    }
}
