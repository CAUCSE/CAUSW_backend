package net.causw.application.dto.util.dtoMapper;

import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.userCouncilFee.UserCouncilFee;
import net.causw.application.dto.board.BoardOfCircleResponseDto;
import net.causw.application.dto.circle.*;
import net.causw.application.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.application.dto.util.dtoMapper.custom.UuidFileToUrlDtoMapper;
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
@Mapping(target = "leaderId", expression = "java(circle.getLeader().map(User::getId).orElse(null))")
@Mapping(target = "leaderName", expression = "java(circle.getLeader().map(User::getName).orElse(null))")
@interface CircleCommonWriterMappings {}

@Mapper(componentModel = "spring")
public interface CircleDtoMapper extends UuidFileToUrlDtoMapper {

    CircleDtoMapper INSTANCE = Mappers.getMapper(CircleDtoMapper.class);

    // User
    UserResponseDto toUserResponseDto(User user);

    // Circle
    @CircleCommonWriterMappings
    @Mapping(target = "mainImage", source = "circle.circleMainImage", qualifiedByName = "mapUuidFileToFileUrl")
    CircleResponseDto toCircleResponseDto(Circle circle);


    @CircleCommonWriterMappings
    @Mapping(target = "mainImage", source = "circle.circleMainImage", qualifiedByName = "mapUuidFileToFileUrl")
    CircleResponseDto toCircleResponseDtoExtended(Circle circle, Long numMember);


    @CircleCommonWriterMappings
    @Mapping(target = "mainImage", source = "circle.circleMainImage", qualifiedByName = "mapUuidFileToFileUrl")
    @Mapping(target = "isJoined", constant = "false")
    @Mapping(target = "isDeleted", source = "circle.isDeleted")
    CirclesResponseDto toCirclesResponseDto(Circle circle, Long numMember);


    @CircleCommonWriterMappings
    @Mapping(target = "mainImage", source = "circle.circleMainImage", qualifiedByName = "mapUuidFileToFileUrl")
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

    @Mapping(target = "id", source = "circle.id")
    @Mapping(target = "name", source = "circle.name")
    @Mapping(target = "description", source = "circle.description")
    @Mapping(target = "isDeleted", source = "circle.isDeleted")
    @Mapping(target = "leaderId", source = "leader.id")
    @Mapping(target = "leaderName", source = "leader.name")
    @Mapping(target = "createdAt", source = "circle.createdAt")
    @Mapping(target = "mainImage", source = "circle.circleMainImage", qualifiedByName = "mapUuidFileToFileUrl")
    CircleResponseDto toCircleResponseDto(Circle circle, User leader);

    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "nickname", source = "user.nickname")
    @Mapping(target = "admissionYear", source = "user.admissionYear")
    @Mapping(target = "studentId", source = "user.studentId")
    @Mapping(target = "major", source = "user.major")
    @Mapping(target = "phoneNumber", source = "user.phoneNumber")
    @Mapping(target = "academicStatus", source = "user.academicStatus")
    @Mapping(target = "currentSemester", source = "user.currentCompletedSemester")
    @Mapping(target = "graduationYear", source = "user.graduationYear")
    @Mapping(target = "graduationType", source = "user.graduationType")
    @Mapping(target = "createdAt", source = "user.createdAt")
    @Mapping(target = "isAppliedThisSemester", source = "isAppliedThisSemester")
    @Mapping(target = "paidAt", source = "userCouncilFee.paidAt")
    @Mapping(target = "paidSemester", source = "userCouncilFee.numOfPaidSemester")
    @Mapping(target = "appliedSemester", source = "appliedSemester")
    @Mapping(target = "restOfSemester", source = "restOfSemester")
    @Mapping(target = "isRefunded", source = "userCouncilFee.isRefunded")
    ExportCircleMemberToExcelResponseDto toExportCircleMemberToExcelResponseDto(
            User user,
            UserCouncilFee userCouncilFee,
            Integer restOfSemester,
            Boolean isAppliedThisSemester,
            Integer appliedSemester
    );

}
