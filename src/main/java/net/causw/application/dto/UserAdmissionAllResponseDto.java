package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.UserAdmissionDomainModel;
import net.causw.domain.model.UserState;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class UserAdmissionAllResponseDto {
    private String id;
    private String userName;
    private UserState userState;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserAdmissionAllResponseDto(
            String id,
            String userName,
            UserState userState,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.userName = userName;
        this.userState = userState;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserAdmissionAllResponseDto from(UserAdmissionDomainModel userAdmissionDomainModel) {
        return new UserAdmissionAllResponseDto(
                userAdmissionDomainModel.getId(),
                userAdmissionDomainModel.getUser().getName(),
                userAdmissionDomainModel.getUser().getState(),
                userAdmissionDomainModel.getCreatedAt(),
                userAdmissionDomainModel.getUpdatedAt()
        );
    }
}
