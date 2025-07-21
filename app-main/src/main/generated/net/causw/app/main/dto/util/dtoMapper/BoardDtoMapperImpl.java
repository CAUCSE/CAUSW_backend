package net.causw.app.main.dto.util.dtoMapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.board.BoardApply;
import net.causw.app.main.domain.model.entity.notification.UserBoardSubscribe;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.dto.board.BoardMainResponseDto;
import net.causw.app.main.dto.board.BoardNameCheckResponseDto;
import net.causw.app.main.dto.board.BoardNameCheckResponseDto.BoardNameCheckResponseDtoBuilder;
import net.causw.app.main.dto.board.BoardOfCircleResponseDto;
import net.causw.app.main.dto.board.BoardOfCircleResponseDto.BoardOfCircleResponseDtoBuilder;
import net.causw.app.main.dto.board.BoardResponseDto;
import net.causw.app.main.dto.board.BoardResponseDto.BoardResponseDtoBuilder;
import net.causw.app.main.dto.board.BoardSubscribeResponseDto;
import net.causw.app.main.dto.board.NormalBoardAppliesResponseDto;
import net.causw.app.main.dto.board.NormalBoardAppliesResponseDto.NormalBoardAppliesResponseDtoBuilder;
import net.causw.app.main.dto.board.NormalBoardApplyResponseDto;
import net.causw.app.main.dto.board.NormalBoardApplyResponseDto.NormalBoardApplyResponseDtoBuilder;
import net.causw.app.main.dto.circle.CircleResponseDto;
import net.causw.app.main.dto.post.PostContentDto;
import net.causw.app.main.dto.user.UserResponseDto;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-21T16:18:46+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.13 (Homebrew)"
)
@Component
public class BoardDtoMapperImpl implements BoardDtoMapper {

    @Override
    public BoardResponseDto toBoardResponseDto(Board entity, List<String> createRoleList, Boolean writable, String circleId, String circleName) {
        if ( entity == null && createRoleList == null && writable == null && circleId == null && circleName == null ) {
            return null;
        }

        BoardResponseDtoBuilder boardResponseDto = BoardResponseDto.builder();

        if ( entity != null ) {
            boardResponseDto.id( entity.getId() );
            boardResponseDto.name( entity.getName() );
            boardResponseDto.description( entity.getDescription() );
            boardResponseDto.category( entity.getCategory() );
            boardResponseDto.isDeleted( entity.getIsDeleted() );
        }
        if ( createRoleList != null ) {
            List<String> list = createRoleList;
            if ( list != null ) {
                boardResponseDto.createRoleList( new ArrayList<String>( list ) );
            }
        }
        if ( writable != null ) {
            boardResponseDto.writable( writable );
        }
        if ( circleId != null ) {
            boardResponseDto.circleId( circleId );
        }
        if ( circleName != null ) {
            boardResponseDto.circleName( circleName );
        }

        return boardResponseDto.build();
    }

    @Override
    public BoardNameCheckResponseDto toBoardNameCheckResponseDto(Boolean isPresent) {
        if ( isPresent == null ) {
            return null;
        }

        BoardNameCheckResponseDtoBuilder boardNameCheckResponseDto = BoardNameCheckResponseDto.builder();

        boardNameCheckResponseDto.isPresent( isPresent );

        return boardNameCheckResponseDto.build();
    }

    @Override
    public BoardOfCircleResponseDto toBoardOfCircleResponseDto(Board board, Post post, Long numComment, boolean writable) {
        if ( board == null && post == null && numComment == null ) {
            return null;
        }

        BoardOfCircleResponseDtoBuilder boardOfCircleResponseDto = BoardOfCircleResponseDto.builder();

        if ( board != null ) {
            boardOfCircleResponseDto.id( board.getId() );
            boardOfCircleResponseDto.name( board.getName() );
            boardOfCircleResponseDto.isDeleted( board.getIsDeleted() );
        }
        if ( post != null ) {
            boardOfCircleResponseDto.postId( post.getId() );
            boardOfCircleResponseDto.postTitle( post.getTitle() );
            boardOfCircleResponseDto.postWriterName( postWriterName( post ) );
            boardOfCircleResponseDto.postWriterStudentId( postWriterStudentId( post ) );
            boardOfCircleResponseDto.postCreatedAt( post.getCreatedAt() );
        }
        if ( numComment != null ) {
            boardOfCircleResponseDto.postNumComment( numComment );
        }
        boardOfCircleResponseDto.writable( writable );

        return boardOfCircleResponseDto.build();
    }

    @Override
    public BoardMainResponseDto toBoardMainResponseDto(Board board, List<PostContentDto> postContentDtos) {
        if ( board == null && postContentDtos == null ) {
            return null;
        }

        BoardMainResponseDto boardMainResponseDto = new BoardMainResponseDto();

        if ( board != null ) {
            boardMainResponseDto.setBoardId( board.getId() );
            boardMainResponseDto.setBoardName( board.getName() );
            boardMainResponseDto.setIsDefault( board.getIsDefault() );
        }
        if ( postContentDtos != null ) {
            List<PostContentDto> list = postContentDtos;
            if ( list != null ) {
                boardMainResponseDto.setContents( new ArrayList<PostContentDto>( list ) );
            }
        }

        return boardMainResponseDto;
    }

    @Override
    public BoardOfCircleResponseDto toBoardOfCircleResponseDto(Board entity, Long numComment, boolean writable) {
        if ( entity == null && numComment == null ) {
            return null;
        }

        BoardOfCircleResponseDtoBuilder boardOfCircleResponseDto = BoardOfCircleResponseDto.builder();

        if ( entity != null ) {
            boardOfCircleResponseDto.id( entity.getId() );
            boardOfCircleResponseDto.name( entity.getName() );
            boardOfCircleResponseDto.isDeleted( entity.getIsDeleted() );
        }
        if ( numComment != null ) {
            boardOfCircleResponseDto.postNumComment( numComment );
        }
        boardOfCircleResponseDto.writable( writable );

        return boardOfCircleResponseDto.build();
    }

    @Override
    public NormalBoardApplyResponseDto toNormalBoardApplyResponseDto(BoardApply boardApply, UserResponseDto user, CircleResponseDto circleResponseDto) {
        if ( boardApply == null && user == null && circleResponseDto == null ) {
            return null;
        }

        NormalBoardApplyResponseDtoBuilder normalBoardApplyResponseDto = NormalBoardApplyResponseDto.builder();

        if ( boardApply != null ) {
            normalBoardApplyResponseDto.id( boardApply.getId() );
            normalBoardApplyResponseDto.boardName( boardApply.getBoardName() );
            normalBoardApplyResponseDto.description( boardApply.getDescription() );
            normalBoardApplyResponseDto.createRoles( boardApply.getCreateRoles() );
            normalBoardApplyResponseDto.isAnonymousAllowed( boardApply.getIsAnonymousAllowed() );
        }
        if ( user != null ) {
            normalBoardApplyResponseDto.user( user );
        }
        if ( circleResponseDto != null ) {
            normalBoardApplyResponseDto.circleResponseDto( circleResponseDto );
        }

        return normalBoardApplyResponseDto.build();
    }

    @Override
    public NormalBoardAppliesResponseDto toNormalBoardAppliesResponseDto(BoardApply boardApply) {
        if ( boardApply == null ) {
            return null;
        }

        NormalBoardAppliesResponseDtoBuilder normalBoardAppliesResponseDto = NormalBoardAppliesResponseDto.builder();

        normalBoardAppliesResponseDto.id( boardApply.getId() );
        normalBoardAppliesResponseDto.boardName( boardApply.getBoardName() );

        return normalBoardAppliesResponseDto.build();
    }

    @Override
    public BoardSubscribeResponseDto toBoardSubscribeResponseDto(UserBoardSubscribe userBoardSubscribe) {
        if ( userBoardSubscribe == null ) {
            return null;
        }

        BoardSubscribeResponseDto boardSubscribeResponseDto = new BoardSubscribeResponseDto();

        boardSubscribeResponseDto.setBoardId( userBoardSubscribeBoardId( userBoardSubscribe ) );
        boardSubscribeResponseDto.setUserId( userBoardSubscribeUserId( userBoardSubscribe ) );
        boardSubscribeResponseDto.setIsSubscribed( userBoardSubscribe.getIsSubscribed() );

        return boardSubscribeResponseDto;
    }

    private String postWriterName(Post post) {
        if ( post == null ) {
            return null;
        }
        User writer = post.getWriter();
        if ( writer == null ) {
            return null;
        }
        String name = writer.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String postWriterStudentId(Post post) {
        if ( post == null ) {
            return null;
        }
        User writer = post.getWriter();
        if ( writer == null ) {
            return null;
        }
        String studentId = writer.getStudentId();
        if ( studentId == null ) {
            return null;
        }
        return studentId;
    }

    private String userBoardSubscribeBoardId(UserBoardSubscribe userBoardSubscribe) {
        if ( userBoardSubscribe == null ) {
            return null;
        }
        Board board = userBoardSubscribe.getBoard();
        if ( board == null ) {
            return null;
        }
        String id = board.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String userBoardSubscribeUserId(UserBoardSubscribe userBoardSubscribe) {
        if ( userBoardSubscribe == null ) {
            return null;
        }
        User user = userBoardSubscribe.getUser();
        if ( user == null ) {
            return null;
        }
        String id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
