package net.causw.application.dto.util.dtoMapper;

import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.uuidFile.joinEntity.PostAttachImage;
import net.causw.application.dto.comment.CommentResponseDto;
import net.causw.application.dto.form.response.FormResponseDto;
import net.causw.application.dto.post.*;
import net.causw.application.dto.util.dtoMapper.custom.UuidFileToUrlDtoMapper;
import net.causw.application.dto.vote.VoteResponseDto;
import net.causw.domain.model.enums.user.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

// Custom Annotation을 사용하여 중복되는 @Mapping을 줄일 수 있습니다.
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
@Mapping(target = "writerName", source = "post.writer.name")
@Mapping(target = "writerAdmissionYear", source = "post.writer.admissionYear")
@Mapping(target = "writerProfileImage", source = "post.writer.profileImage")
@interface CommonPostWriterMappings {
}

@Mapper(componentModel = "spring")
public interface PostDtoMapper extends UuidFileToUrlDtoMapper {

    PostDtoMapper INSTANCE = Mappers.getMapper(PostDtoMapper.class);

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
    PostsResponseDto toPostsResponseDto(Post post, Long numComment, Long numPostLike, Long numPostFavorite, PostAttachImage thumbnail, Boolean isPostVote, Boolean isPostForm);

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
    PostResponseDto toPostResponseDtoExtended(
            Post post,
            Page<CommentResponseDto> commentList,
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
            Boolean isPostForm
    );

    @Mapping(target = "title", source = "post.title")
    @Mapping(target = "contentId", source = "post.id")
    PostContentDto toPostContentDto(Post post);

    @Mapping(target = "boardId", source = "board.id")
    @Mapping(target = "boardName", source = "board.name")
    BoardPostsResponseDto toBoardPostsResponseDto(Board board, Set<Role> userRole, Boolean writable, Boolean isFavorite, Page<PostsResponseDto> post);

    @Mapping(target="id", source = "post.id")
    PostCreateResponseDto toPostCreateResponseDto(Post post);
}
