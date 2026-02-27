package net.causw.app.main.domain.community.post.api.v1.mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import net.causw.app.main.domain.asset.file.entity.joinEntity.PostAttachImage;
import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.form.api.v1.dto.response.FormResponseDto;
import net.causw.app.main.domain.community.post.api.v1.dto.BoardPostsResponseDto;
import net.causw.app.main.domain.community.post.api.v1.dto.PostContentDto;
import net.causw.app.main.domain.community.post.api.v1.dto.PostCreateResponseDto;
import net.causw.app.main.domain.community.post.api.v1.dto.PostResponseDto;
import net.causw.app.main.domain.community.post.api.v1.dto.PostSubscribeResponseDto;
import net.causw.app.main.domain.community.post.api.v1.dto.PostsResponseDto;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.query.PostQueryResult;
import net.causw.app.main.domain.community.vote.api.v1.dto.VoteResponseDto;
import net.causw.app.main.domain.notification.notification.entity.UserPostSubscribe;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.shared.dto.util.dtoMapper.custom.UuidFileToUrlDtoMapper;
import net.causw.global.constant.StaticValue;

// Custom Annotation을 사용하여 중복되는 @Mapping을 줄일 수 있습니다.
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
@Mapping(target = "writerName", source = "post.writer.name")
@Mapping(target = "writerAdmissionYear", source = "post.writer.admissionYear")
@Mapping(target = "writerProfileImage", source = "post.writer.profileImage")
@interface CommonPostWriterMappings{}

@Mapper(componentModel = "spring")
public interface PostDtoV1Mapper extends UuidFileToUrlDtoMapper {

	PostDtoV1Mapper INSTANCE = Mappers.getMapper(PostDtoV1Mapper.class);

	// Dto writerName 필드에 post.writer.name을 삽입한다는 의미입니다.
	@Mapping(target = "id", source = "post.id")
	@Mapping(target = "writerName", source = "post.writer.name")
	@Mapping(target = "writerNickname", source = "post.writer.nickname")
	@Mapping(target = "writerAdmissionYear", source = "post.writer.admissionYear")
	@Mapping(target = "content", source = "post.content")
	@Mapping(target = "isAnonymous", source = "post.isAnonymous")
	@Mapping(target = "isQuestion", source = "post.isQuestion")
	@Mapping(target = "createdAt", source = "post.createdAt")
	@Mapping(target = "updatedAt", source = "post.updatedAt")
	@Mapping(target = "numLike", source = "numPostLike")
	@Mapping(target = "numFavorite", source = "numPostFavorite")
	@Mapping(target = "postAttachImage", source = "thumbnail", qualifiedByName = "mapUuidFileToFileUrl")
	@Mapping(target = "isPostVote", source = "isPostVote")
	@Mapping(target = "isPostForm", source = "isPostForm")
	PostsResponseDto toPostsResponseDto(Post post, Long numComment, Long numPostLike, Long numPostFavorite,
		PostAttachImage thumbnail, Boolean isPostVote, Boolean isPostForm);

	@Mapping(target = "id", source = "queryResult.postId")
	@Mapping(target = "title", source = "queryResult.title")
	@Mapping(target = "writerName", source = "queryResult.writerName")
	@Mapping(target = "writerNickname", source = "queryResult.writerNickname")
	@Mapping(target = "writerAdmissionYear", source = "queryResult.writerAdmissionYear")
	@Mapping(target = "numComment", source = "queryResult.numComment")
	@Mapping(target = "numLike", source = "queryResult.numLike")
	@Mapping(target = "numFavorite", source = "queryResult.numFavorite")
	@Mapping(target = "isAnonymous", source = "queryResult.isAnonymous")
	@Mapping(target = "isQuestion", source = "queryResult.isQuestion")
	@Mapping(target = "createdAt", source = "queryResult.createdAt")
	@Mapping(target = "updatedAt", source = "queryResult.updatedAt")
	@Mapping(target = "isDeleted", source = "queryResult.isDeleted")
	@Mapping(target = "postAttachImage", source = "queryResult.postAttachImage")
	@Mapping(target = "isPostVote", source = "queryResult.isPostVote")
	@Mapping(target = "isPostForm", source = "queryResult.isPostForm")
	@Mapping(target = "displayWriterNickname", expression = "java(getDisplayWriterNickname(queryResult.hasWriter(), queryResult.writerUserState(), queryResult.writerDeletedAt(), queryResult.isAnonymous(), queryResult.writerNickname()))")
	PostsResponseDto toPostsResponseDto(PostQueryResult queryResult);

	default String getDisplayWriterNickname(
		boolean hasWriter,
		UserState state,
		LocalDateTime writerDeletedAt,
		boolean isAnonymous,
		String nickname) {
		if (hasWriter &&
			(writerDeletedAt != null || List.of(UserState.INACTIVE, UserState.DROP).contains(state))) {
			return StaticValue.INACTIVE_USER_NICKNAME;

		} else if (isAnonymous) {
			return StaticValue.ANONYMOUS_USER_NICKNAME;

		} else {
			return nickname;
		}
	}

	@Mapping(target = "title", source = "post.title")
	@Mapping(target = "writerName", source = "post.writer.name")
	@Mapping(target = "writerNickname", source = "post.writer.nickname")
	@Mapping(target = "writerAdmissionYear", source = "post.writer.admissionYear")
	@Mapping(target = "boardName", source = "post.board.name")
	@Mapping(target = "fileUrlList", source = "post.postAttachImageList", qualifiedByName = "mapUuidFileListToFileUrlList")
	@Mapping(target = "content", source = "post.content")
	@Mapping(target = "isAnonymous", source = "post.isAnonymous")
	@Mapping(target = "isQuestion", source = "post.isQuestion")
	@Mapping(target = "numLike", source = "numPostLike")
	@Mapping(target = "numFavorite", source = "numPostFavorite")
	@Mapping(target = "isPostLike", source = "isPostLike")
	@Mapping(target = "isPostFavorite", source = "isPostFavorite")
	@Mapping(target = "isOwner", source = "isOwner")
	@Mapping(target = "updatable", source = "updatable")
	@Mapping(target = "deletable", source = "deletable")
	@Mapping(target = "writerProfileImage", source = "post.writer.userProfileImage", qualifiedByName = "mapUuidFileToFileUrl")
	@Mapping(target = "formResponseDto", source = "formResponseDto")
	@Mapping(target = "voteResponseDto", source = "voteResponseDto")
	@Mapping(target = "isPostVote", source = "isPostVote")
	@Mapping(target = "isPostForm", source = "isPostForm")
	@Mapping(target = "isPostSubscribed", source = "isPostSubscribed")
	PostResponseDto toPostResponseDtoExtended(
		Post post,
		Long numComment,
		Long numPostLike,
		Long numPostFavorite,
		Boolean isPostLike,
		Boolean isPostFavorite,
		Boolean isOwner,
		Boolean updatable,
		Boolean deletable,
		FormResponseDto formResponseDto,
		VoteResponseDto voteResponseDto,
		Boolean isPostVote,
		Boolean isPostForm,
		Boolean isPostSubscribed);

	@Mapping(target = "title", source = "post.title")
	@Mapping(target = "contentId", source = "post.id")
	@Mapping(target = "writerNickname", source = "post.writer.nickname")
	@Mapping(target = "createdAt", source = "post.createdAt")
	@Mapping(target = "isAnonymous", source = "post.isAnonymous")
	PostContentDto toPostContentDto(Post post);

	@Mapping(target = "boardId", source = "board.id")
	@Mapping(target = "boardName", source = "board.name")
	BoardPostsResponseDto toBoardPostsResponseDto(Board board, Set<Role> userRole, Boolean writable, Boolean isFavorite,
		Boolean isBoardSubscribed, Page<PostsResponseDto> post);

	@Mapping(target = "id", source = "post.id")
	PostCreateResponseDto toPostCreateResponseDto(Post post);

	@Mapping(target = "postId", source = "post.id")
	@Mapping(target = "userId", source = "user.id")
	@Mapping(target = "isSubscribed", source = "isSubscribed")
	PostSubscribeResponseDto toPostSubscribeResponseDto(UserPostSubscribe userPostSubscribe);
}
