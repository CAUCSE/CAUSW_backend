package net.causw.application.dto.util;

import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.board.BoardOfCircleResponseDto;
import net.causw.application.dto.circle.CircleBoardsResponseDto;
import net.causw.application.dto.circle.CircleMemberResponseDto;
import net.causw.application.dto.circle.CircleResponseDto;
import net.causw.application.dto.circle.CirclesResponseDto;
import net.causw.application.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.application.dto.user.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDateTime;
import java.util.List;

// Custom Annotation을 사용하여 중복되는 @Mapping을 줄일 수 있습니다.
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
@Mapping(target = "leaderId", expression = "java(circle.getLeader().map(User::getId).orElse(null))")
@Mapping(target = "leaderName", expression = "java(circle.getLeader().map(User::getName).orElse(null))")
@interface CircleCommonWriterMappings {}

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
@Mapping(target = "mainImage", expression = "java(circle.getUuidFile().getFileUrl().orElse(null))")
@interface CircleMainImageWriterMappings {}


@Mapper(componentModel = "spring")
public interface CircleServiceDtoMapper {

    CircleServiceDtoMapper INSTANCE = Mappers.getMapper(CircleServiceDtoMapper.class);

    @Named("mapUuidFileToFileUrl")
    default String mapUuidFileToFileUrl(Circle circle) {
        return circle.getUuidFile().getFileUrl();
    }

    // User
    UserResponseDto toUserResponseDto(User user);

    // Circle
    @CircleCommonWriterMappings
    @CircleMainImageWriterMappings
    CircleResponseDto toCircleResponseDto(Circle circle);


    @CircleCommonWriterMappings
    @CircleMainImageWriterMappings
    CircleResponseDto toCircleResponseDtoExtended(Circle circle, Long numMember);


    @CircleCommonWriterMappings
    @CircleMainImageWriterMappings
    @Mapping(target = "isJoined", constant = "false")
    @Mapping(target = "isDeleted", source = "circle.isDeleted")
    CirclesResponseDto toCirclesResponseDto(Circle circle, Long numMember);


    @CircleCommonWriterMappings
    @CircleMainImageWriterMappings
    @Mapping(target = "isJoined", constant = "true")
    @Mapping(target = "isDeleted", source = "circle.isDeleted")
    CirclesResponseDto toCirclesResponseDtoExtended(Circle circle, Long numMember, LocalDateTime joinedAt);


    @Mapping(target = "postNumComment", constant = "0L")
    BoardOfCircleResponseDto toBoardOfCircleResponseDto(Board board, Boolean writeable);

    @Mapping(target = "id", source = "board.id")
    @Mapping(target = "isDeleted", source = "board.isDeleted")
    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "postTitle", source = "post.title")
    @Mapping(target = "postWriterName", source = "post.writer.name")
    @Mapping(target = "postWriterStudentId", source = "post.writer.studentId")
    @Mapping(target = "postCreatedAt", source = "post.createdAt")
    BoardOfCircleResponseDto toBoardOfCircleResponseDtoExtended(Board board, Boolean writeable, Post post, Long postNumComment);

    CircleBoardsResponseDto toCircleBoardsResponseDto(CircleResponseDto circle, List<BoardOfCircleResponseDto> boardList);

    @Mapping(target = "id", source = "board.id")
    CircleMemberResponseDto toCircleMemberResponseDto(CircleMember board, CircleResponseDto circle, UserResponseDto user);

    DuplicatedCheckResponseDto toDuplicatedCheckResponseDto(Boolean result);

}
