package net.causw.app.main.dto.comment;

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
    private String writerName;

    @Schema(description = "대댓글 작성자 닉네임", example = "푸앙이")
    private String writerNickname;

    private Integer writerAdmissionYear;
    private String writerProfileImage;
    private Boolean updatable;
    private Boolean deletable;

    @Schema(description = "익명글 여부", example = "False")
    private Boolean isAnonymous;

    @Schema(description = "대댓글 작성자 여부", example = "False")
    private Boolean isOwner;

    @Schema(description = "로그인한 유저가 좋아요를 눌렀는 지 여부", example ="False")
    private Boolean isChildCommentLike;

    @Schema(description = "대댓글 종아요 수", example = "10")
    private Long numLike;
}
