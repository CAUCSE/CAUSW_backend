package net.causw.application.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import net.causw.application.dto.comment.CommentResponseDto;
import net.causw.application.dto.form.response.FormResponseDto;
import net.causw.application.dto.vote.VoteResponseDto;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class PostResponseDto {
    @Schema(description = "게시글 id", example = "uuid 형식의 String 값입니다.")
    private String id;

    @Schema(description = "게시글 제목", example = "게시글의 제목입니다.")
    private String title;

    @Schema(description = "게시글 내용", example = "안녕하세요. 학생회입니다. 공지사항입니다.")
    private String content;

    @Schema(description = "게시글 삭제여부", example = "false")
    private Boolean isDeleted;

    @Schema(description = "게시글 작성자 이름", example = "관리자")
    private String writerName;

    @Schema(description = "게시글 작성자 닉네임", example = "푸앙이")
    private String writerNickname;

    @Schema(description = "게시글 작성자의 승인년도", example = "2020")
    private Integer writerAdmissionYear;

    @Schema(description = "게시글 작성자의 프로필 이미지", example = "프로필 이미지 url 작성")
    private String writerProfileImage;

    @Schema(description = "첨부파일", example = "첨부파일 url 작성")
    private List<String> fileUrlList;

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

    @Schema(description = "게시글 작성자 여부", example = "False")
    private Boolean isOwner;

    @Schema(description = "로그인한 사용자가 좋아요를 이미 누른지 여부", example = "False")
    private Boolean isPostLike;

    @Schema(description = "로그인한 사용자가 즐겨찾기를 이미 누른지 여부", example = "False")
    private Boolean isPostFavorite;

    @Schema(description = "해당 글이 투표가 포함됐는지 여부", example = "False")
    private Boolean isPostVote;

    @Schema(description = "해당 글이 설문이 포함됐는지 여부", example = "False")
    private Boolean isPostForm;

    @Schema(description = "게시글 업데이트 가능여부", example = "true")
    private Boolean updatable;

    @Schema(description = "게시글 삭제 가능여부", example = "true")
    private Boolean deletable;

    @Schema(description = "게시글 생성 시간", example = "2024-01-26T18:40:40.643Z")
    private LocalDateTime createdAt;

    @Schema(description = "게시글 업데이트 시간", example = "2024-01-26T18:40:40.643Z")
    private LocalDateTime updatedAt;

    @Schema(description = "게시글의 답글 정보", example = "답글에 대한 정보 조회")
    private Page<CommentResponseDto> commentList;

    @Schema(description = "게시판 이름", example = "게시판 이름입니다.")
    private String boardName;

    @Schema(description = "게시글의 신청서 정보")
    private FormResponseDto formResponseDto;

    @Schema(description = "투표 정보")
    private VoteResponseDto voteResponseDto;
}
