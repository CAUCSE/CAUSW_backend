package net.causw.application.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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

    @Schema(description = "게시글 제목", example = "공지사항입니다.")
    @NotBlank(message = "게시글 제목을 입력해 주세요.")
    private String title;

    @Schema(description = "게시글 내용", example = "안녕하세요. 학생회입니다. 공지사항입니다.")
    @NotBlank(message = "게시글 내용을 입력해 주세요.")
    private String content;

    @Schema(description = "게시판 id", example = "uuid 형식의 String 값입니다.")
    @NotBlank // 게시판 id
    private String boardId;

    @Schema(description = "첨부파일", example = "첨부파일 url 작성")
    private List<String> attachmentList;

    public List<String> getAttachmentList() {
        return Optional.ofNullable(this.attachmentList).orElse(List.of());
    }
}
