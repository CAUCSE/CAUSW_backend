package net.causw.app.main.domain.community.comment.api.v2.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.ChildCommentResponseDto;
import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.global.constant.StaticValue;

@Mapper(componentModel = "spring")
public interface ChildCommentResponseDtoMapper {

	@Mapping(target = "writerName", expression = "java(childComment.getIsAnonymous() ? null : childComment.getWriter().getName())")
	@Mapping(target = "writerNickname", expression = "java(childComment.getIsAnonymous() ? null : childComment.getWriter().getNickname())")
	@Mapping(target = "displayWriterNickname", expression = "java(mapDisplayNicknameForChildComment(childComment, childComment.getWriter().getNickname()))")
	@Mapping(target = "writerAdmissionYear", expression = "java(childComment.getIsAnonymous() ? null : childComment.getWriter().getAdmissionYear())")
	@Mapping(target = "writerProfileImage", expression = "java(mapProfileImageForChildComment(childComment))")
	@Mapping(target = "isAnonymous", source = "childComment.isAnonymous")
	@Mapping(target = "numLike", source = "numChildCommentLike")
	@Mapping(target = "isBlocked", source = "isBlocked")
	@Mapping(target = "content", expression = "java(mapContentForChildComment(childComment.getContent(), isBlocked))")
	ChildCommentResponseDto toChildCommentResponseDto(
		ChildComment childComment,
		Long numChildCommentLike,
		Boolean isChildCommentLike,
		Boolean isOwner,
		Boolean updatable,
		Boolean deletable,
		Boolean isBlocked);

	default String mapContentForChildComment(String content, Boolean isBlocked) {
		return Boolean.TRUE.equals(isBlocked) ? null : content;
	}

	default String mapProfileImageForChildComment(ChildComment childComment) {
		if (Boolean.TRUE.equals(childComment.getIsAnonymous())) {
			return null;
		}

		UserProfileImage userProfileImage = childComment.getWriter().getUserProfileImage();
		if (userProfileImage == null) {
			return null;
		} else {
			return userProfileImage.getUuidFile() == null ? null
				: userProfileImage.getUuidFile().getFileUrl();
		}
	}

	default String mapDisplayNicknameForChildComment(ChildComment childComment, String originalNickname) {
		User writer = childComment.getWriter();
		if (writer != null && List.of(UserState.INACTIVE, UserState.DROP, UserState.DELETED)
			.contains(writer.getState())) {
			return StaticValue.INACTIVE_USER_NICKNAME;
		} else if (Boolean.TRUE.equals(childComment.getIsAnonymous())) {
			return StaticValue.ANONYMOUS_USER_NICKNAME;
		} else {
			return originalNickname;
		}
	}
}
