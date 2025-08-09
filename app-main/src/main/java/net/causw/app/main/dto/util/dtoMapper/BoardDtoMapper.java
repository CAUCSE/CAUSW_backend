package net.causw.app.main.dto.util.dtoMapper;

import net.causw.app.main.dto.post.PostContentDto;
import net.causw.app.main.dto.user.UserResponseDto;
import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.board.BoardApply;
import net.causw.app.main.domain.model.entity.notification.UserBoardSubscribe;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.dto.board.*;
import net.causw.app.main.dto.circle.CircleResponseDto;
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
    @Mapping(target = "circleResponseDto", source = "circleResponseDto")
    BoardApplyResponseDto toBoardApplyResponseDto(BoardApply boardApply, UserResponseDto user, CircleResponseDto circleResponseDto);

    @Mapping(target = "id", source = "boardApply.id")
    @Mapping(target = "boardName", source = "boardApply.boardName")
    BoardAppliesResponseDto toBoardAppliesResponseDto(BoardApply boardApply);



    @Mapping(target = "boardId", source = "userBoardSubscribe.board.id")
    @Mapping(target = "userId", source = "userBoardSubscribe.user.id")
    @Mapping(target = "isSubscribed", source = "userBoardSubscribe.isSubscribed")
    BoardSubscribeResponseDto toBoardSubscribeResponseDto(UserBoardSubscribe userBoardSubscribe);


}
