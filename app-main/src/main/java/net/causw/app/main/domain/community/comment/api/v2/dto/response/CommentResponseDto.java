package net.causw.app.main.domain.community.comment.api.v2.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record CommentResponseDto(
	String id,
	String content,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	Boolean isDeleted,
	String postId,
	String writerName,

	@Schema(description = "댓글 작성자 닉네임", example = "푸앙이") String writerNickname,

	@Schema(description = "표시될 댓글 작성자 닉네임", example = "[닉네임/비활성 유저/익명]") String displayWriterNickname,

	@Schema(description = "작성자의 입학연도", example = "2022") Integer writerAdmissionYear,

	@Schema(description = "작성자 사진이 저장되어 있는 URL 주소(없으면 Null 반환)", example = "http://test/123") String writerProfileImage,

	Boolean updatable,
	Boolean deletable,

	@Schema(description = "차단된 컨텐츠 여부", example = "False") Boolean isBlocked,

	@Schema(description = "익명글 여부", example = "False") Boolean isAnonymous,

	@Schema(description = "댓글 작성자 여부", example = "False") Boolean isOwner,

	@Schema(description = "로그인한 유저가 댓글에 좋아요를 이미 누른지 여부", example = "False") Boolean isCommentLike,

	@Schema(description = "댓글 구독 여부") Boolean isCommentSubscribed,

	@Schema(description = "댓글 종아요 수", example = "10") Long numLike,

	@Schema(description = "대댓글 수", example = "5") Long numChildComment,

	@Schema(description = "대댓글 DTO 리스트", example = "대댓글 DTO 리스트 입니다.") List<ChildCommentResponseDto> childCommentList) {

}
