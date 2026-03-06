package net.causw.app.main.domain.community.comment.api.v2.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.shared.dto.ProfileImageDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ChildCommentResponseDto(
	String id,
	String content,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	Boolean isDeleted,
	String writerName,

	@Schema(description = "대댓글 작성자 닉네임", example = "푸앙이") String writerNickname,

	@Schema(description = "표시될 대댓글 작성자 닉네임", example = "[닉네임/비활성 유저/익명]") String displayWriterNickname,

	Integer writerAdmissionYear,

	@Schema(description = "작성자 프로필 이미지 정보 (익명인 경우 null)") ProfileImageDto writerProfileImage,

	Boolean updatable,
	Boolean deletable,

	@Schema(description = "차단된 컨텐츠 여부", example = "False") Boolean isBlocked,

	@Schema(description = "익명글 여부", example = "False") Boolean isAnonymous,

	@Schema(description = "대댓글 작성자 여부", example = "False") Boolean isOwner,

	@Schema(description = "로그인한 유저가 좋아요를 눌렀는 지 여부", example = "False") Boolean isChildCommentLike,

	@Schema(description = "대댓글 좋아요 수", example = "10") Long numLike) {

}
