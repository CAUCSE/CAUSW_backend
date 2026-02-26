package net.causw.app.main.domain.community.comment.api.v2.mapper;

import java.util.List;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.ChildCommentResponseDto;
import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.global.constant.StaticValue;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChildCommentResponseDtoMapper {

	/**
	 * ChildComment 엔티티와 관련 데이터를 ChildCommentResponseDto로 변환합니다.
	 * 차단된 콘텐츠, 탈퇴/정지 유저, 익명 작성자에 대한 정보 보호 로직을 포함합니다.
	 */
	public static ChildCommentResponseDto toChildCommentResponseDto(
		ChildComment childComment,
		Long numChildCommentLike,
		Boolean isChildCommentLike,
		Boolean isOwner,
		Boolean updatable,
		Boolean deletable,
		Boolean isBlocked) {

		User writer = childComment.getWriter();

		String content = Boolean.TRUE.equals(isBlocked) ? null : childComment.getContent();

		String displayWriterNickname;
		boolean isInactiveUser = writer != null && List.of(UserState.INACTIVE, UserState.DROP, UserState.DELETED)
			.contains(writer.getState());

		if (isInactiveUser) {
			displayWriterNickname = StaticValue.INACTIVE_USER_NICKNAME;
		} else if (Boolean.TRUE.equals(childComment.getIsAnonymous())) {
			displayWriterNickname = StaticValue.ANONYMOUS_USER_NICKNAME;
		} else {
			displayWriterNickname = writer != null ? writer.getNickname() : null;
		}

		String writerName = null;
		String writerNickname = null;
		Integer writerAdmissionYear = null;
		String writerProfileImage = null;

		if (!Boolean.TRUE.equals(childComment.getIsAnonymous()) && writer != null) {
			writerName = writer.getName();
			writerNickname = writer.getNickname();
			writerAdmissionYear = writer.getAdmissionYear();

			UserProfileImage userProfileImage = writer.getUserProfileImage();
			if (userProfileImage != null && userProfileImage.getUuidFile() != null) {
				writerProfileImage = userProfileImage.getUuidFile().getFileUrl();
			}
		}

		return ChildCommentResponseDto.builder()
			.id(childComment.getId())
			.content(content)

			.writerName(writerName)
			.writerNickname(writerNickname)
			.displayWriterNickname(displayWriterNickname)
			.writerAdmissionYear(writerAdmissionYear)
			.writerProfileImage(writerProfileImage)

			.isAnonymous(childComment.getIsAnonymous())
			.numLike(numChildCommentLike)
			.isChildCommentLike(isChildCommentLike)
			.isOwner(isOwner)

			.updatable(updatable)
			.deletable(deletable)
			.isBlocked(isBlocked)

			.createdAt(childComment.getCreatedAt())
			.updatedAt(childComment.getUpdatedAt())
			.build();
	}
}