package net.causw.application.dto.util.dtoMapper;

import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.board.BoardApply;
import net.causw.adapter.persistence.post.Post;
import net.causw.application.dto.board.*;
import net.causw.application.dto.post.PostContentDto;
import net.causw.application.dto.user.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BoardDtoMapper {

    BoardDtoMapper INSTANCE = Mappers.getMapper(BoardDtoMapper.class);

    BoardResponseDto toBoardResponseDto(Board entity, List<String> createRoleList, Boolean writable, String circleId, String circleName);

    @Mapping(target = "isPresent", source = "isPresent")
    BoardNameCheckResponseDto toBoardNameCheckResponseDto(Boolean isPresent);

    @Mapping(target = "id", source = "board.id")
    @Mapping(target = "name", source = "board.name")
    @Mapping(target = "writable", source = "writable")
    @Mapping(target = "isDeleted", source = "board.isDeleted")
    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "postTitle", source = "post.title")
    @Mapping(target = "postWriterName", source = "post.writer.name")
    @Mapping(target = "postWriterStudentId", source = "post.writer.studentId")
    @Mapping(target = "postCreatedAt", source = "post.createdAt")
    @Mapping(target = "postNumComment", source = "numComment")
    BoardOfCircleResponseDto toBoardOfCircleResponseDto(Board board, Post post, Long numComment, boolean writable);

    @Mapping(target = "boardId", source = "board.id")
    @Mapping(target = "boardName", source = "board.name")
    @Mapping(target = "isDefault", source = "board.isDefault")
    @Mapping(target = "contents", source = "postContentDtos")
    BoardMainResponseDto toBoardMainResponseDto(Board board, List<PostContentDto> postContentDtos);

    @Mapping(target = "writable", source = "writable")
    @Mapping(target = "postNumComment", source = "numComment")
    BoardOfCircleResponseDto toBoardOfCircleResponseDto(Board entity, Long numComment, boolean writable);

    @Mapping(target = "id", source = "boardApply.id")
    @Mapping(target = "boardName", source = "boardApply.boardName")
    @Mapping(target = "description", source = "boardApply.description")
    @Mapping(target = "createRoles", source = "boardApply.createRoles")
    @Mapping(target = "isAnonymousAllowed", source = "boardApply.isAnonymousAllowed")
    @Mapping(target = "user", source = "user")
    NormalBoardApplyResponseDto toNormalBoardApplyResponseDto(BoardApply boardApply, UserResponseDto user);

    @Mapping(target = "id", source = "boardApply.id")
    @Mapping(target = "boardName", source = "boardApply.boardName")
    NormalBoardAppliesResponseDto toNormalBoardAppliesResponseDto(BoardApply boardApply);



}
