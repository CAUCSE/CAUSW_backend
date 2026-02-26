package net.causw.app.main.domain.community.comment.api.v2.mapper;

import java.util.List;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.ChildCommentResponseDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.CommentResponseDto;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.global.constant.StaticValue;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentResponseDtoMapper {

	/**
	 * Comment 엔티티와 관련 데이터를 CommentResponseDto로 변환합니다.
	 * 차단된 콘텐츠, 탈퇴/정지 유저, 익명 게시글에 대한 정보 보호 로직을 포함합니다.
	 */
	public static CommentResponseDto toCommentResponseDto(
		Comment comment,
		Long numChildComment,
		Long numCommentLike,
		Boolean isCommentLike,
		Boolean isOwner,
		List<ChildCommentResponseDto> childCommentList,
		Boolean updatable,
		Boolean deletable,
		Boolean isCommentSubscribed,
		Boolean isBlocked) {

		User writer = comment.getWriter();

		String content = Boolean.TRUE.equals(isBlocked) ? null : comment.getContent();

		String displayWriterNickname;
		boolean isInactiveUser = writer != null && List.of(UserState.INACTIVE, UserState.DROP, UserState.DELETED)
			.contains(writer.getState());

		if (isInactiveUser) {
			displayWriterNickname = StaticValue.INACTIVE_USER_NICKNAME;
		} else if (Boolean.TRUE.equals(comment.getIsAnonymous())) {
			displayWriterNickname = StaticValue.ANONYMOUS_USER_NICKNAME;
		} else {
			displayWriterNickname = writer != null ? writer.getNickname() : null;
		}

		String writerName = null;
		String writerNickname = null;
		Integer writerAdmissionYear = null;
		String writerProfileImage = null;

		if (!Boolean.TRUE.equals(comment.getIsAnonymous()) && writer != null) {
			writerName = writer.getName();
			writerNickname = writer.getNickname();
			writerAdmissionYear = writer.getAdmissionYear();

			UserProfileImage userProfileImage = writer.getUserProfileImage();
			if (userProfileImage != null && userProfileImage.getUuidFile() != null) {
				writerProfileImage = userProfileImage.getUuidFile().getFileUrl();
			}
		}

		return CommentResponseDto.builder()
			.id(comment.getId())
			.content(content)
			.postId(comment.getPost().getId())

			// 작성자 정보
			.writerName(writerName)
			.writerNickname(writerNickname)
			.displayWriterNickname(displayWriterNickname)
			.writerAdmissionYear(writerAdmissionYear)
			.writerProfileImage(writerProfileImage)

			// 댓글 메타 데이터
			.isAnonymous(comment.getIsAnonymous())
			.numChildComment(numChildComment)
			.numLike(numCommentLike)
			.isCommentLike(isCommentLike)
			.isOwner(isOwner)
			.childCommentList(childCommentList)

			// 권한 및 상태
			.updatable(updatable)
			.deletable(deletable)
			.isCommentSubscribed(isCommentSubscribed)
			.isBlocked(isBlocked)

			// 공통 시간 필드
			.createdAt(comment.getCreatedAt())
			.updatedAt(comment.getUpdatedAt())
			.build();
	}
}