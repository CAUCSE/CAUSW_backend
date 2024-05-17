package net.causw.application.dto.user;

import io.swagger.annotations.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.user.UserAdmissionDomainModel;
import net.causw.domain.model.user.UserDomainModel;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserAdmissionResponseDto {

    @ApiModelProperty(value = "승인 고유 id 값", example = "uuid 형식의 String 값입니다.", required = true)
    private String id;

    private UserResponseDto user;

    private String attachImage;

    @ApiModelProperty(value = "자기소개 글 (255자 이내)", example = "안녕하세요! 코딩을 좋아하는 신입생 이예빈입니다.")
    private String description;

    @ApiModelProperty(value = "생선된 시각", example = "2024-01-24T00:26:40.643Z")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "마지막 업데이트된 시각", example = "2024-01-24T00:26:40.643Z")
    private LocalDateTime updatedAt;

    public static UserAdmissionResponseDto from(UserAdmissionDomainModel userAdmissionDomainModel) {
        return UserAdmissionResponseDto.builder()
                .id(userAdmissionDomainModel.getId())
                .user(UserResponseDto.from(userAdmissionDomainModel.getUser()))
                .attachImage(userAdmissionDomainModel.getAttachImage())
                .description(userAdmissionDomainModel.getDescription())
                .createdAt(userAdmissionDomainModel.getCreatedAt())
                .updatedAt(userAdmissionDomainModel.getUpdatedAt())
                .build();
    }

    public static UserAdmissionResponseDto of(
            UserAdmissionDomainModel userAdmissionDomainModel,
            UserDomainModel user
    ) {
        return UserAdmissionResponseDto.builder()
                .id(userAdmissionDomainModel.getId())
                .user(UserResponseDto.from(user))
                .attachImage(userAdmissionDomainModel.getAttachImage())
                .description(userAdmissionDomainModel.getDescription())
                .createdAt(userAdmissionDomainModel.getCreatedAt())
                .updatedAt(userAdmissionDomainModel.getUpdatedAt())
                .build();
    }
}
