package net.causw.app.main.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormalBoardApplyRequestDto {

    @NotBlank(message = "게시판 이름은 필수 입력 값입니다.")
    @Schema(description = "게시판 이름", example = "백준 질문 게시판")
    private String boardName;

    @Schema(description = "게시판 설명", example = "백준 문제에 대한 질문을 올리는 게시판입니다.")
    private String description;

    @NotNull(message = "익명 허용 여부는 필수 입력 값입니다.")
    @Schema(description = "익명 허용 여부", example = "true -> boolean으로 받아야함")
    private Boolean isAnonymousAllowed;

    @Schema(description = "게시판이 속한 동아리 id입니다. 만약 동아리 게시판이 없을 시 null로 보내면 됩니다.", example = "uuid 형식의 String 값입니다(nullable).", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String circleId;

}
