package net.causw.app.main.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PostCreateRequestDto {

    @NotBlank(message = "게시글 제목을 입력해 주세요.")
    @Schema(description = "게시글 제목", example = "공지사항입니다.")
    private String title;

    @NotBlank(message = "게시글 내용을 입력해 주세요.")
    @Schema(description = "게시글 내용", example = "안녕하세요. 학생회입니다. 공지사항입니다.")
    private String content;

    @NotBlank(message = "게시판 id를 입력해 주세요.")
    @Schema(description = "게시판 id", example = "uuid 형식의 String 값입니다.")
    private String boardId;

    @NotNull(message = "익명글 여부를 선택해 주세요.")
    @Schema(description = "익명글 여부", example = "False")
    private Boolean isAnonymous;

    @NotNull(message = "질문글 여부를 선택해 주세요.")
    @Schema(description = "질문글 여부", example = "False")
    private Boolean isQuestion;

}
