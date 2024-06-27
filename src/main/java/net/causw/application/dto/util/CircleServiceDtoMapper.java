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
@Mapping(target = "leaderId", expression = "java(entity.getLeader().map(User::getId).orElse(null))")
@Mapping(target = "leaderName", expression = "java(entity.getLeader().map(User::getName).orElse(null))")
@interface CircleCommonWriterMappings {}

@Mapper(componentModel = "spring")
public interface CircleServiceDtoMapper {

    CircleServiceDtoMapper INSTANCE = Mappers.getMapper(CircleServiceDtoMapper.class);


    // User
    UserResponseDto toUserResponseDto(User entity);

    // Circle


    @CircleCommonWriterMappings
    CircleResponseDto toCircleResponseDto(Circle entity);


    @CircleCommonWriterMappings
    CircleResponseDto toCircleResponseDtoExtended(Circle entity, Long numMember);


    @CircleCommonWriterMappings
    @Mapping(target = "isJoined", constant = "false")
    CirclesResponseDto toCirclesResponseDto(Circle entity, Long numMember);


    @CircleCommonWriterMappings
    @Mapping(target = "isJoined", constant = "true")
    CirclesResponseDto toCirclesResponseDtoExtended(Circle entity, Long numMember, LocalDateTime joinedAt);


    @Mapping(target = "postNumComment", constant = "0L")
    BoardOfCircleResponseDto toBoardOfCircleResponseDto(Board entity, Boolean writeable);

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "isDeleted", source = "entity.isDeleted")
    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "postTitle", source = "post.title")
    @Mapping(target = "postWriterName", source = "post.writer.name")
    @Mapping(target = "postWriterStudentId", source = "post.writer.studentId")
    @Mapping(target = "postCreatedAt", source = "post.createdAt")
    BoardOfCircleResponseDto toBoardOfCircleResponseDtoExtended(Board entity, Boolean writeable, Post post, Long postNumComment);

    CircleBoardsResponseDto toCircleBoardsResponseDto(CircleResponseDto circle, List<BoardOfCircleResponseDto> boardList);

    @Mapping(target = "id", source = "entity.id")
    CircleMemberResponseDto toCircleMemberResponseDto(CircleMember entity, CircleResponseDto circle, UserResponseDto user);

    DuplicatedCheckResponseDto toDuplicatedCheckResponseDto(Boolean result);

}
