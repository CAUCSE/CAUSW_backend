package net.causw.application.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    @Schema(description = "첨부파일", example = "첨부파일 url 작성")
    private List<String> attachmentList;

    @NotNull(message = "익명글 여부를 선택해 주세요.")
    @Schema(description = "익명글 여부", example = "False")
    private Boolean isAnonymous;

    @NotNull(message = "질문글 여부를 선택해 주세요.")
    @Schema(description = "질문글 여부", example = "False")
    private Boolean isQuestion;

    public List<String> getAttachmentList() {
        return Optional.ofNullable(this.attachmentList).orElse(List.of());
    }
}
