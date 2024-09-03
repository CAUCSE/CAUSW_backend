package net.causw.application.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormalBoardCreateRequestDto {
    @NotBlank
    @NotNull
    @Schema(description = "게시판 이름", example = "백준 질문 게시판")
    private String boardName;
}
