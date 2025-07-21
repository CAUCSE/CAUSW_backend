package net.causw.app.main.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class EventResponseDto {

    @Schema(description = "이벤트 id", example = "uuid 형식의 String 값입니다.")
    private String id;

    @Schema(description = "이벤트 URL")
    private String url;

    @Schema(description = "이미지", example = "")
    private String image;

    @Schema(description = "삭제 여부", example = "false")
    private Boolean isDeleted;

    @Schema(description = "이벤트 생성 시간", example = "2024.01.26.")
    private String createdAt;

    @Schema(description = "이벤트 업데이트 시간", example = "2024.01.26.")
    private String updatedAt;
}
