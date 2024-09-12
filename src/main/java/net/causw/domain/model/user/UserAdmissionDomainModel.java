package net.causw.domain.model.user;

import lombok.Builder;
import lombok.Getter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.causw.adapter.persistence.uuidFile.UuidFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class UserAdmissionDomainModel {
    private String id;

    private List<UuidFile> uuidFileList;

    @Size(max = 255, message = "소개글은 255글자이상으로 작성할 수 없습니다.")
    private String description;

    @NotNull(message = "사용자가 입력되지 않았습니다.")
    private UserDomainModel user;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static UserAdmissionDomainModel of(
            String id,
            UserDomainModel user,
            List<UuidFile> uuidFileList,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return UserAdmissionDomainModel.builder()
                .id(id)
                .user(user)
                .uuidFileList(uuidFileList)
                .description(description)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static UserAdmissionDomainModel of(
            UserDomainModel user,
            List<UuidFile> uuidFileList,
            String description
    ) {
        return UserAdmissionDomainModel.builder()
                .user(user)
                .uuidFileList(uuidFileList)
                .description(description)
                .build();
    }
}
