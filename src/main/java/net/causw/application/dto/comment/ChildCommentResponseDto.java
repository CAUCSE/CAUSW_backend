package net.causw.application.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ChildCommentResponseDto {
    private String id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private String tagUserName;
    private String refChildComment;
    private String writerName;
    private Integer writerAdmissionYear;
    private String writerProfileImage;
    private Boolean updatable;
    private Boolean deletable;

    @Schema(description = "익명글 여부", example = "False")
    private Boolean isAnonymous;

    @Schema(description = "대댓글 종아요 수", example = "10")
    private Long numLike;

}
