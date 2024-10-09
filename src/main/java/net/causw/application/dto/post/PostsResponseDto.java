package net.causw.application.dto.post;

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
public class
PostsResponseDto {
    @Schema(description = "게시글 id", example = "uuid 형식의 String 값입니다.")
    private String id;

    @Schema(description = "게시글 제목", example = "게시글의 제목입니다.")
    private String title;

    @Schema(description = "게시글 내용", example = "게시글의 내용입니다.")
    private String content;

    @Schema(description = "게시글 작성자 이름", example = "관리자")
    private String writerName;

    @Schema(description = "게시글 작성자의 승인년도", example = "2020")
    private Integer writerAdmissionYear;

    @Schema(description = "답글 개수", example = "13")
    private Long numComment;

    @Schema(description = "게시글 종아요 개수", example = "10")
    private Long numLike;

    @Schema(description = "게시글 즐겨찾기 개수", example = "11")
    private Long numFavorite;

    @Schema(description = "익명글 여부", example = "False")
    private Boolean isAnonymous;

    @Schema(description = "질문글 여부", example = "False")
    private Boolean isQuestion;

    @Schema(description = "게시글 생성 시간", example = "2024-01-26T18:40:40.643Z")
    private LocalDateTime createdAt;

    @Schema(description = "게시글 업데이트 시간", example = "2024-01-26T18:40:40.643Z")
    private LocalDateTime updatedAt;

    @Schema(description = "게시글 삭제여부", example = "false")
    private Boolean isDeleted;

    @Schema(description = "이미지 url", example = "")
    private String postAttachImage;

    @Schema(description = "투표 포함 여부" ,example = "false")
    private Boolean isPostVote;

    @Schema(description = "게시글 신청서 존재 여부", example = "true")
    private Boolean isPostForm;

}
