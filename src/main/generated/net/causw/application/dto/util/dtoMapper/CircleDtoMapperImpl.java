package net.causw.application.dto.util.dtoMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import net.causw.domain.model.enums.Role;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-09-13T05:58:23+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.12 (Azul Systems, Inc.)"
)
@Component
public class CircleDtoMapperImpl implements CircleDtoMapper {

    @Override
    public UserResponseDto toUserResponseDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponseDtoBuilder userResponseDto = UserResponseDto.builder();

        userResponseDto.id( user.getId() );
        userResponseDto.email( user.getEmail() );
        userResponseDto.name( user.getName() );
        userResponseDto.studentId( user.getStudentId() );
        userResponseDto.admissionYear( user.getAdmissionYear() );
        Set<Role> set = user.getRoles();
        if ( set != null ) {
            userResponseDto.roles( new HashSet<Role>( set ) );
        }
        userResponseDto.state( user.getState() );
        userResponseDto.nickname( user.getNickname() );
        userResponseDto.major( user.getMajor() );
        userResponseDto.academicStatus( user.getAcademicStatus() );
        userResponseDto.currentCompletedSemester( user.getCurrentCompletedSemester() );
        userResponseDto.graduationYear( user.getGraduationYear() );
        userResponseDto.graduationType( user.getGraduationType() );
        userResponseDto.phoneNumber( user.getPhoneNumber() );

        return userResponseDto.build();
    }

    @Override
    public CircleResponseDto toCircleResponseDto(Circle circle) {
        if ( circle == null ) {
            return null;
        }

        CircleResponseDtoBuilder circleResponseDto = CircleResponseDto.builder();

        circleResponseDto.mainImage( mapUuidFileToFileUrl( circle.getCircleMainImageUuidFile() ) );
        circleResponseDto.id( circle.getId() );
        circleResponseDto.name( circle.getName() );
        circleResponseDto.description( circle.getDescription() );
        circleResponseDto.isDeleted( circle.getIsDeleted() );
        circleResponseDto.circleTax( circle.getCircleTax() );
        circleResponseDto.recruitMembers( circle.getRecruitMembers() );
        circleResponseDto.createdAt( circle.getCreatedAt() );
        circleResponseDto.recruitEndDate( circle.getRecruitEndDate() );
        circleResponseDto.isRecruit( circle.getIsRecruit() );

        circleResponseDto.leaderId( circle.getLeader().map(User::getId).orElse(null) );
        circleResponseDto.leaderName( circle.getLeader().map(User::getName).orElse(null) );

        return circleResponseDto.build();
    }

    @Override
    public CircleResponseDto toCircleResponseDtoExtended(Circle circle, Long numMember) {
        if ( circle == null && numMember == null ) {
            return null;
        }

        CircleResponseDtoBuilder circleResponseDto = CircleResponseDto.builder();

        if ( circle != null ) {
            circleResponseDto.mainImage( mapUuidFileToFileUrl( circle.getCircleMainImageUuidFile() ) );
            circleResponseDto.id( circle.getId() );
            circleResponseDto.name( circle.getName() );
            circleResponseDto.description( circle.getDescription() );
            circleResponseDto.isDeleted( circle.getIsDeleted() );
            circleResponseDto.circleTax( circle.getCircleTax() );
            circleResponseDto.recruitMembers( circle.getRecruitMembers() );
            circleResponseDto.createdAt( circle.getCreatedAt() );
            circleResponseDto.recruitEndDate( circle.getRecruitEndDate() );
            circleResponseDto.isRecruit( circle.getIsRecruit() );
        }
        if ( numMember != null ) {
            circleResponseDto.numMember( numMember );
        }
        circleResponseDto.leaderId( circle.getLeader().map(User::getId).orElse(null) );
        circleResponseDto.leaderName( circle.getLeader().map(User::getName).orElse(null) );

        return circleResponseDto.build();
    }

    @Override
    public CirclesResponseDto toCirclesResponseDto(Circle circle, Long numMember) {
        if ( circle == null && numMember == null ) {
            return null;
        }

        CirclesResponseDtoBuilder circlesResponseDto = CirclesResponseDto.builder();

        if ( circle != null ) {
            circlesResponseDto.mainImage( mapUuidFileToFileUrl( circle.getCircleMainImageUuidFile() ) );
            circlesResponseDto.isDeleted( circle.getIsDeleted() );
            circlesResponseDto.id( circle.getId() );
            circlesResponseDto.name( circle.getName() );
            circlesResponseDto.description( circle.getDescription() );
            circlesResponseDto.createdAt( circle.getCreatedAt() );
            circlesResponseDto.recruitEndDate( circle.getRecruitEndDate() );
            circlesResponseDto.isRecruit( circle.getIsRecruit() );
        }
        if ( numMember != null ) {
            circlesResponseDto.numMember( numMember );
        }
        circlesResponseDto.leaderId( circle.getLeader().map(User::getId).orElse(null) );
        circlesResponseDto.leaderName( circle.getLeader().map(User::getName).orElse(null) );
        circlesResponseDto.isJoined( false );

        return circlesResponseDto.build();
    }

    @Override
    public CirclesResponseDto toCirclesResponseDtoExtended(Circle circle, Long numMember, LocalDateTime joinedAt) {
        if ( circle == null && numMember == null && joinedAt == null ) {
            return null;
        }

        CirclesResponseDtoBuilder circlesResponseDto = CirclesResponseDto.builder();

        if ( circle != null ) {
            circlesResponseDto.mainImage( mapUuidFileToFileUrl( circle.getCircleMainImageUuidFile() ) );
            circlesResponseDto.isDeleted( circle.getIsDeleted() );
            circlesResponseDto.id( circle.getId() );
            circlesResponseDto.name( circle.getName() );
            circlesResponseDto.description( circle.getDescription() );
            circlesResponseDto.createdAt( circle.getCreatedAt() );
            circlesResponseDto.recruitEndDate( circle.getRecruitEndDate() );
            circlesResponseDto.isRecruit( circle.getIsRecruit() );
        }
        if ( numMember != null ) {
            circlesResponseDto.numMember( numMember );
        }
        if ( joinedAt != null ) {
            circlesResponseDto.joinedAt( joinedAt );
        }
        circlesResponseDto.leaderId( circle.getLeader().map(User::getId).orElse(null) );
        circlesResponseDto.leaderName( circle.getLeader().map(User::getName).orElse(null) );
        circlesResponseDto.isJoined( true );

        return circlesResponseDto.build();
    }

    @Override
    public BoardOfCircleResponseDto toBoardOfCircleResponseDto(Board board, Boolean writeable) {
        if ( board == null && writeable == null ) {
            return null;
        }

        BoardOfCircleResponseDtoBuilder boardOfCircleResponseDto = BoardOfCircleResponseDto.builder();

        if ( board != null ) {
            boardOfCircleResponseDto.id( board.getId() );
            boardOfCircleResponseDto.name( board.getName() );
            boardOfCircleResponseDto.isDeleted( board.getIsDeleted() );
        }
        boardOfCircleResponseDto.postNumComment( (long) 0L );

        return boardOfCircleResponseDto.build();
    }

    @Override
    public BoardOfCircleResponseDto toBoardOfCircleResponseDtoExtended(Board board, Boolean writeable, Post post, Long postNumComment) {
        if ( board == null && writeable == null && post == null && postNumComment == null ) {
            return null;
        }

        BoardOfCircleResponseDtoBuilder boardOfCircleResponseDto = BoardOfCircleResponseDto.builder();

        if ( board != null ) {
            boardOfCircleResponseDto.id( board.getId() );
            boardOfCircleResponseDto.isDeleted( board.getIsDeleted() );
            boardOfCircleResponseDto.name( board.getName() );
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
    public CircleMemberResponseDto toCircleMemberResponseDto(CircleMember board, CircleResponseDto circle, UserResponseDto user) {
        if ( board == null && circle == null && user == null ) {
            return null;
        }

        CircleMemberResponseDtoBuilder circleMemberResponseDto = CircleMemberResponseDto.builder();

        if ( board != null ) {
            circleMemberResponseDto.id( board.getId() );
            circleMemberResponseDto.status( board.getStatus() );
            circleMemberResponseDto.circle( toCircleResponseDto( board.getCircle() ) );
            circleMemberResponseDto.user( toUserResponseDto( board.getUser() ) );
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

    @Override
    public CircleResponseDto toCircleResponseDto(Circle circle, User leader) {
        if ( circle == null && leader == null ) {
            return null;
        }

        CircleResponseDtoBuilder circleResponseDto = CircleResponseDto.builder();

        if ( circle != null ) {
            circleResponseDto.id( circle.getId() );
            circleResponseDto.name( circle.getName() );
            circleResponseDto.description( circle.getDescription() );
            circleResponseDto.isDeleted( circle.getIsDeleted() );
            circleResponseDto.createdAt( circle.getCreatedAt() );
            circleResponseDto.circleTax( circle.getCircleTax() );
            circleResponseDto.recruitMembers( circle.getRecruitMembers() );
            circleResponseDto.recruitEndDate( circle.getRecruitEndDate() );
            circleResponseDto.isRecruit( circle.getIsRecruit() );
        }
        if ( leader != null ) {
            circleResponseDto.leaderId( leader.getId() );
            circleResponseDto.leaderName( leader.getName() );
        }

        return circleResponseDto.build();
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
