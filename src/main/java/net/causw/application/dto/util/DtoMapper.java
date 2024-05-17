package net.causw.application.dto.util;

import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.comment.ChildComment;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.post.Post;
import net.causw.application.dto.comment.ChildCommentResponseDto;
import net.causw.application.dto.comment.CommentResponseDto;
import net.causw.application.dto.file.FileResponseDto;
import net.causw.application.dto.post.BoardPostsResponseDto;
import net.causw.application.dto.post.PostResponseDto;
import net.causw.application.dto.post.PostsResponseDto;
import net.causw.domain.model.enums.Role;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// Custom Annotation을 사용하여 중복되는 @Mapping을 줄일 수 있습니다.
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
@Mapping(target = "writerName", source = "entity.writer.name")
@Mapping(target = "writerAdmissionYear", source = "entity.writer.admissionYear")
@Mapping(target = "writerProfileImage", source = "entity.writer.profileImage")
@interface CommonWriterMappings {}

@Mapper(componentModel = "spring")
public interface DtoMapper{

    DtoMapper INSTANCE = Mappers.getMapper(DtoMapper.class);

    // 자료형 변환 등이 필요하다면 아래 형식으로 메서드를 작성합니다.
    // 이 메서드는 post.attachment를 attachmentsToStringList 메서드로 List<FileResponseDto>로 변환합니다.
    // 메서드 수가 많아지면 별도의 Converter 클래스를 만들어 상속받는 식으로 처리해도 좋습니다.
    @Named("attachmentsToStringList")
    default List<FileResponseDto> attachmentsToStringList(String attachments) {
        if(attachments == null || attachments.isEmpty()) return List.of();
        return Arrays.stream(attachments.split(":::"))
                .map(FileResponseDto::from)
                .collect(Collectors.toList());
    }

    // Dto writerName 필드에 post.writer.name을 삽입한다는 의미입니다.
    @Mapping(target = "writerName", source = "entity.writer.name")
    @Mapping(target = "writerAdmissionYear", source = "entity.writer.admissionYear")
    PostsResponseDto toPostsResponseDto(Post entity, Long numComment);

    @CommonWriterMappings
    @Mapping(target = "boardName", source = "entity.board.name")
    @Mapping(target = "attachmentList", source = "entity.attachments", qualifiedByName = "attachmentsToStringList")
    PostResponseDto toPostResponseDto(Post entity, Boolean updatable, Boolean deletable);

    @CommonWriterMappings
    @Mapping(target = "boardName", source = "entity.board.name")
    @Mapping(target = "attachmentList", source = "entity.attachments", qualifiedByName = "attachmentsToStringList")
    @Mapping(target = "content", source = "entity.content")
    PostResponseDto toPostResponseDtoExtended(Post entity, Page<CommentResponseDto> commentList, Long numComment, Boolean updatable, Boolean deletable);

    @CommonWriterMappings
    @Mapping(target = "postId", source = "entity.post.id")
    CommentResponseDto toCommentResponseDto(Comment entity, Long numChildComment, List<ChildCommentResponseDto> childCommentList, Boolean updatable, Boolean deletable);

    @CommonWriterMappings
    ChildCommentResponseDto toChildCommentResponseDto(ChildComment entity, Boolean updatable, Boolean deletable);

    @Mapping(target = "boardId", source = "entity.id")
    @Mapping(target = "boardName", source = "entity.name")
    BoardPostsResponseDto toBoardPostsResponseDto(Board entity, Role userRole, Boolean writable, Boolean isFavorite, Page<PostsResponseDto> post);

    /** TODO: 각자 역할분담한 부분의 Dto를 위를 참고하여 아래 작성하시면 됩니다.
     *  기존에 Dto에 존재하던 of 메서드를 DtoMapper.INSTANCE.toDtoName(entity)로 대체하시면 됩니다.
     *  컴파일 후 DtoMapperImpl 파일을 확인하여 필드별로 제대로 매핑이 되었는지 확인해야 합니다.
     */

    // User


    // Board


    // Circle


    // Locker


}
