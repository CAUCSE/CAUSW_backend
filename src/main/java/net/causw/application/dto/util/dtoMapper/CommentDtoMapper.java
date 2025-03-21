package net.causw.application.dto.util.dtoMapper;

import net.causw.adapter.persistence.comment.ChildComment;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.notification.UserCommentSubscribe;
import net.causw.application.dto.comment.ChildCommentResponseDto;
import net.causw.application.dto.comment.CommentResponseDto;
import net.causw.application.dto.comment.CommentSubscribeResponseDto;
import net.causw.application.dto.util.dtoMapper.custom.UuidFileToUrlDtoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentDtoMapper extends UuidFileToUrlDtoMapper {

    CommentDtoMapper INSTANCE = Mappers.getMapper(CommentDtoMapper.class);

    @Mapping(target = "writerName", source = "comment.writer.name")
    @Mapping(target ="writerNickname", source = "comment.writer.nickname")
    @Mapping(target = "writerAdmissionYear", source = "comment.writer.admissionYear")
    @Mapping(target = "writerProfileImage", source = "comment.writer.userProfileImage", qualifiedByName = "mapUuidFileToFileUrl")
    @Mapping(target = "postId", source = "comment.post.id")
    @Mapping(target = "isAnonymous", source = "comment.isAnonymous")
    @Mapping(target ="numLike", source = "numCommentLike")
    @Mapping(target = "childCommentList", source = "childCommentList")
    CommentResponseDto toCommentResponseDto(
            Comment comment,
            Long numChildComment,
            Long numCommentLike,
            Boolean isCommentLike,
            Boolean isOwner,
            List<ChildCommentResponseDto> childCommentList,
            Boolean updatable,
            Boolean deletable);

    @Mapping(target = "writerName", source = "childComment.writer.name")
    @Mapping(target ="writerNickname", source = "childComment.writer.nickname")
    @Mapping(target = "writerAdmissionYear", source = "childComment.writer.admissionYear")
    @Mapping(target = "writerProfileImage", source = "childComment.writer.userProfileImage", qualifiedByName = "mapUuidFileToFileUrl")
    @Mapping(target = "isAnonymous", source = "childComment.isAnonymous")
    @Mapping(target ="numLike", source = "numChildCommentLike")
    ChildCommentResponseDto toChildCommentResponseDto(
            ChildComment childComment,
            Long numChildCommentLike,
            Boolean isChildCommentLike,
            Boolean isOwner,
            Boolean updatable,
            Boolean deletable);



    @Mapping(target = "commentId", source = "comment.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "isSubscribed", source = "isSubscribed")
    CommentSubscribeResponseDto toCommentSubscribeResponseDto(UserCommentSubscribe userCommentSubscribe);

}
