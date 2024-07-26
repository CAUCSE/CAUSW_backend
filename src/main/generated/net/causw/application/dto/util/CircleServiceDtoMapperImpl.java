package net.causw.application.dto.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.board.BoardOfCircleResponseDto;
import net.causw.application.dto.board.BoardOfCircleResponseDto.BoardOfCircleResponseDtoBuilder;
import net.causw.application.dto.circle.CircleBoardsResponseDto;
import net.causw.application.dto.circle.CircleBoardsResponseDto.CircleBoardsResponseDtoBuilder;
import net.causw.application.dto.circle.CircleMemberResponseDto;
import net.causw.application.dto.circle.CircleMemberResponseDto.CircleMemberResponseDtoBuilder;
import net.causw.application.dto.circle.CircleResponseDto;
import net.causw.application.dto.circle.CircleResponseDto.CircleResponseDtoBuilder;
import net.causw.application.dto.circle.CirclesResponseDto;
import net.causw.application.dto.circle.CirclesResponseDto.CirclesResponseDtoBuilder;
import net.causw.application.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.application.dto.duplicate.DuplicatedCheckResponseDto.DuplicatedCheckResponseDtoBuilder;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.application.dto.user.UserResponseDto.UserResponseDtoBuilder;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-07-25T10:41:57+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.12 (Amazon.com Inc.)"
)
@Component
public class CircleServiceDtoMapperImpl implements CircleServiceDtoMapper {

    @Override
    public UserResponseDto toUserResponseDto(User entity) {
        if ( entity == null ) {
            return null;
        }

        UserResponseDtoBuilder userResponseDto = UserResponseDto.builder();

        userResponseDto.id( entity.getId() );
        userResponseDto.email( entity.getEmail() );
        userResponseDto.name( entity.getName() );
        userResponseDto.studentId( entity.getStudentId() );
        userResponseDto.admissionYear( entity.getAdmissionYear() );
        userResponseDto.role( entity.getRole() );
        userResponseDto.profileImage( entity.getProfileImage() );
        userResponseDto.state( entity.getState() );

        return userResponseDto.build();
    }

    @Override
    public CircleResponseDto toCircleResponseDto(Circle entity) {
        if ( entity == null ) {
            return null;
        }

        CircleResponseDtoBuilder circleResponseDto = CircleResponseDto.builder();

        circleResponseDto.id( entity.getId() );
        circleResponseDto.name( entity.getName() );
        circleResponseDto.mainImage( entity.getMainImage() );
        circleResponseDto.description( entity.getDescription() );
        circleResponseDto.isDeleted( entity.getIsDeleted() );
        circleResponseDto.createdAt( entity.getCreatedAt() );

        circleResponseDto.leaderId( entity.getLeader().map(User::getId).orElse(null) );
        circleResponseDto.leaderName( entity.getLeader().map(User::getName).orElse(null) );

        return circleResponseDto.build();
    }

    @Override
    public CircleResponseDto toCircleResponseDtoExtended(Circle entity, Long numMember) {
        if ( entity == null && numMember == null ) {
            return null;
        }

        CircleResponseDtoBuilder circleResponseDto = CircleResponseDto.builder();

        if ( entity != null ) {
            circleResponseDto.id( entity.getId() );
            circleResponseDto.name( entity.getName() );
            circleResponseDto.mainImage( entity.getMainImage() );
            circleResponseDto.description( entity.getDescription() );
            circleResponseDto.isDeleted( entity.getIsDeleted() );
            circleResponseDto.createdAt( entity.getCreatedAt() );
        }
        if ( numMember != null ) {
            circleResponseDto.numMember( numMember );
        }
        circleResponseDto.leaderId( entity.getLeader().map(User::getId).orElse(null) );
        circleResponseDto.leaderName( entity.getLeader().map(User::getName).orElse(null) );

        return circleResponseDto.build();
    }

    @Override
    public CirclesResponseDto toCirclesResponseDto(Circle entity, Long numMember) {
        if ( entity == null && numMember == null ) {
            return null;
        }

        CirclesResponseDtoBuilder circlesResponseDto = CirclesResponseDto.builder();

        if ( entity != null ) {
            circlesResponseDto.id( entity.getId() );
            circlesResponseDto.name( entity.getName() );
            circlesResponseDto.mainImage( entity.getMainImage() );
            circlesResponseDto.description( entity.getDescription() );
            circlesResponseDto.createdAt( entity.getCreatedAt() );
        }
        if ( numMember != null ) {
            circlesResponseDto.numMember( numMember );
        }
        circlesResponseDto.leaderId( entity.getLeader().map(User::getId).orElse(null) );
        circlesResponseDto.leaderName( entity.getLeader().map(User::getName).orElse(null) );
        circlesResponseDto.isJoined( false );

        return circlesResponseDto.build();
    }

    @Override
    public CirclesResponseDto toCirclesResponseDtoExtended(Circle entity, Long numMember, LocalDateTime joinedAt) {
        if ( entity == null && numMember == null && joinedAt == null ) {
            return null;
        }

        CirclesResponseDtoBuilder circlesResponseDto = CirclesResponseDto.builder();

        if ( entity != null ) {
            circlesResponseDto.id( entity.getId() );
            circlesResponseDto.name( entity.getName() );
            circlesResponseDto.mainImage( entity.getMainImage() );
            circlesResponseDto.description( entity.getDescription() );
            circlesResponseDto.createdAt( entity.getCreatedAt() );
        }
        if ( numMember != null ) {
            circlesResponseDto.numMember( numMember );
        }
        if ( joinedAt != null ) {
            circlesResponseDto.joinedAt( joinedAt );
        }
        circlesResponseDto.leaderId( entity.getLeader().map(User::getId).orElse(null) );
        circlesResponseDto.leaderName( entity.getLeader().map(User::getName).orElse(null) );
        circlesResponseDto.isJoined( true );

        return circlesResponseDto.build();
    }

    @Override
    public BoardOfCircleResponseDto toBoardOfCircleResponseDto(Board entity, Boolean writeable) {
        if ( entity == null && writeable == null ) {
            return null;
        }

        BoardOfCircleResponseDtoBuilder boardOfCircleResponseDto = BoardOfCircleResponseDto.builder();

        if ( entity != null ) {
            boardOfCircleResponseDto.id( entity.getId() );
            boardOfCircleResponseDto.name( entity.getName() );
            boardOfCircleResponseDto.isDeleted( entity.getIsDeleted() );
        }
        boardOfCircleResponseDto.postNumComment( (long) 0L );

        return boardOfCircleResponseDto.build();
    }

    @Override
    public BoardOfCircleResponseDto toBoardOfCircleResponseDtoExtended(Board entity, Boolean writeable, Post post, Long postNumComment) {
        if ( entity == null && writeable == null && post == null && postNumComment == null ) {
            return null;
        }

        BoardOfCircleResponseDtoBuilder boardOfCircleResponseDto = BoardOfCircleResponseDto.builder();

        if ( entity != null ) {
            boardOfCircleResponseDto.id( entity.getId() );
            boardOfCircleResponseDto.isDeleted( entity.getIsDeleted() );
            boardOfCircleResponseDto.name( entity.getName() );
        }
        if ( post != null ) {
            boardOfCircleResponseDto.postId( post.getId() );
            boardOfCircleResponseDto.postTitle( post.getTitle() );
            boardOfCircleResponseDto.postWriterName( postWriterName( post ) );
            boardOfCircleResponseDto.postWriterStudentId( postWriterStudentId( post ) );
            boardOfCircleResponseDto.postCreatedAt( post.getCreatedAt() );
        }
        if ( postNumComment != null ) {
            boardOfCircleResponseDto.postNumComment( postNumComment );
        }

        return boardOfCircleResponseDto.build();
    }

    @Override
    public CircleBoardsResponseDto toCircleBoardsResponseDto(CircleResponseDto circle, List<BoardOfCircleResponseDto> boardList) {
        if ( circle == null && boardList == null ) {
            return null;
        }

        CircleBoardsResponseDtoBuilder circleBoardsResponseDto = CircleBoardsResponseDto.builder();

        if ( circle != null ) {
            circleBoardsResponseDto.circle( circle );
        }
        if ( boardList != null ) {
            List<BoardOfCircleResponseDto> list = boardList;
            if ( list != null ) {
                circleBoardsResponseDto.boardList( new ArrayList<BoardOfCircleResponseDto>( list ) );
            }
        }

        return circleBoardsResponseDto.build();
    }

    @Override
    public CircleMemberResponseDto toCircleMemberResponseDto(CircleMember entity, CircleResponseDto circle, UserResponseDto user) {
        if ( entity == null && circle == null && user == null ) {
            return null;
        }

        CircleMemberResponseDtoBuilder circleMemberResponseDto = CircleMemberResponseDto.builder();

        if ( entity != null ) {
            circleMemberResponseDto.id( entity.getId() );
            circleMemberResponseDto.status( entity.getStatus() );
            circleMemberResponseDto.circle( toCircleResponseDto( entity.getCircle() ) );
            circleMemberResponseDto.user( toUserResponseDto( entity.getUser() ) );
        }

        return circleMemberResponseDto.build();
    }

    @Override
    public DuplicatedCheckResponseDto toDuplicatedCheckResponseDto(Boolean result) {
        if ( result == null ) {
            return null;
        }

        DuplicatedCheckResponseDtoBuilder duplicatedCheckResponseDto = DuplicatedCheckResponseDto.builder();

        duplicatedCheckResponseDto.result( result );

        return duplicatedCheckResponseDto.build();
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
}
