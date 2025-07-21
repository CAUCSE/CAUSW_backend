package net.causw.app.main.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class UserAdmissionResponseDto {

    @Schema(description = "승인 고유 id 값", example = "uuid 형식의 String 값입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "사용자 정보")
    private UserResponseDto user;

    @Schema(description = "첨부 이미지")
    private List<String> attachImageUrlList;

    @Schema(description = "자기소개 글 (255자 이내)", example = "안녕하세요! 코딩을 좋아하는 신입생 이예빈입니다.")
    private String description;

    @Schema(description = "생성된 시각", example = "2024-01-24T00:26:40.643Z")
    private LocalDateTime createdAt;

    @Schema(description = "마지막 업데이트된 시각", example = "2024-01-24T00:26:40.643Z")
    private LocalDateTime updatedAt;

    @Schema(description = "거부 사유", example = "양식과 다른 사진을 제출하여 거절되었습니다")
    private String rejectReason;
}
