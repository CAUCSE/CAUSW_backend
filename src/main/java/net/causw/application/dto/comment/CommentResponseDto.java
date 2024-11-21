package net.causw.application.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CommentResponseDto {
    private String id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private String postId;
    private String writerName;

    @Schema(description = "댓글 작성자 닉네임", example = "푸앙이")
    private String writerNickname;

    @Schema(description = "작성자의 입학연도", example = "2022")
    private Integer writerAdmissionYear;

    @Schema(description = "작성자 사진이 저장되어 있는 URL 주소(없으면 Null 반환)", example = "http://test/123")
    private String writerProfileImage;

    private Boolean updatable;
    private Boolean deletable;

    @Schema(description = "익명글 여부", example = "False")
    private Boolean isAnonymous;

    @Schema(description = "댓글 작성자 여부", example = "False")
    private Boolean isOwner;

    @Schema(description = "로그인한 유저가 댓글에 좋아요를 이미 누른지 여부", example = "False")
    private Boolean isCommentLike;

    @Schema(description = "댓글 종아요 수", example = "10")
    private Long numLike;

    @Schema(description = "대댓글 수", example ="5")
    private Long numChildComment;

    @Schema(description = "대댓글 DTO 리스트", example ="대댓글 DTO 리스트 입니다.")
    private List<ChildCommentResponseDto> childCommentList;

}
