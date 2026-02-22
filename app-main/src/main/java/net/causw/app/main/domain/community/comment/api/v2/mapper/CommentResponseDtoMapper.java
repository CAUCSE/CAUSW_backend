package net.causw.app.main.domain.community.comment.api.v2.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.ChildCommentResponseDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.CommentResponseDto;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.global.constant.StaticValue;

@Mapper(componentModel = "spring")
public interface CommentResponseDtoMapper {

	@Mapping(target = "writerName", expression = "java(comment.getIsAnonymous() ? null : comment.getWriter().getName())")
	@Mapping(target = "writerNickname", expression = "java(comment.getIsAnonymous() ? null : comment.getWriter().getNickname())")
	@Mapping(target = "displayWriterNickname", expression = "java(mapDisplayNicknameForComment(comment, comment.getWriter().getNickname()))")
	@Mapping(target = "writerAdmissionYear", expression = "java(comment.getIsAnonymous() ? null : comment.getWriter().getAdmissionYear())")
	@Mapping(target = "writerProfileImage", expression = "java(mapProfileImageForComment(comment))")
	@Mapping(target = "postId", source = "comment.post.id")
	@Mapping(target = "isAnonymous", source = "comment.isAnonymous")
	@Mapping(target = "numLike", source = "numCommentLike")
	@Mapping(target = "childCommentList", source = "childCommentList")
	@Mapping(target = "isCommentSubscribed", source = "isCommentSubscribed")
	@Mapping(target = "isBlocked", source = "isBlocked")
	@Mapping(target = "content", expression = "java(mapContentForComment(comment.getContent(), isBlocked))")
	CommentResponseDto toCommentResponseDto(
		Comment comment,
		Long numChildComment,
		Long numCommentLike,
		Boolean isCommentLike,
		Boolean isOwner,
		List<ChildCommentResponseDto> childCommentList,
		Boolean updatable,
		Boolean deletable,
		Boolean isCommentSubscribed,
		Boolean isBlocked);

	default String mapContentForComment(String content, Boolean isBlocked) {
		return Boolean.TRUE.equals(isBlocked) ? null : content;
	}

	default String mapProfileImageForComment(Comment comment) {
		if (Boolean.TRUE.equals(comment.getIsAnonymous())) {
			return null;
		}

		UserProfileImage userProfileImage = comment.getWriter().getUserProfileImage();
		if (userProfileImage == null) {
			return null;
		} else {
			return userProfileImage.getUuidFile() == null ? null
				: userProfileImage.getUuidFile().getFileUrl();
		}
	}

	default String mapDisplayNicknameForComment(Comment comment, String originalNickname) {
		User writer = comment.getWriter();
		if (writer != null && List.of(UserState.INACTIVE, UserState.DROP, UserState.DELETED)
			.contains(writer.getState())) {
			return StaticValue.INACTIVE_USER_NICKNAME;
		} else if (Boolean.TRUE.equals(comment.getIsAnonymous())) {
			return StaticValue.ANONYMOUS_USER_NICKNAME;
		} else {
			return originalNickname;
		}
	}
}
