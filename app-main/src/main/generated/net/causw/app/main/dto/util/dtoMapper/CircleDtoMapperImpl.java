package net.causw.app.main.dto.util.dtoMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.circle.Circle;
import net.causw.app.main.domain.model.entity.circle.CircleMember;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.userCouncilFee.UserCouncilFee;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.dto.board.BoardOfCircleResponseDto;
import net.causw.app.main.dto.board.BoardOfCircleResponseDto.BoardOfCircleResponseDtoBuilder;
import net.causw.app.main.dto.circle.CircleBoardsResponseDto;
import net.causw.app.main.dto.circle.CircleBoardsResponseDto.CircleBoardsResponseDtoBuilder;
import net.causw.app.main.dto.circle.CircleMemberResponseDto;
import net.causw.app.main.dto.circle.CircleMemberResponseDto.CircleMemberResponseDtoBuilder;
import net.causw.app.main.dto.circle.CircleResponseDto;
import net.causw.app.main.dto.circle.CircleResponseDto.CircleResponseDtoBuilder;
import net.causw.app.main.dto.circle.CirclesResponseDto;
import net.causw.app.main.dto.circle.CirclesResponseDto.CirclesResponseDtoBuilder;
import net.causw.app.main.dto.circle.ExportCircleMemberToExcelResponseDto;
import net.causw.app.main.dto.circle.ExportCircleMemberToExcelResponseDto.ExportCircleMemberToExcelResponseDtoBuilder;
import net.causw.app.main.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.app.main.dto.duplicate.DuplicatedCheckResponseDto.DuplicatedCheckResponseDtoBuilder;
import net.causw.app.main.dto.user.UserResponseDto;
import net.causw.app.main.dto.user.UserResponseDto.UserResponseDtoBuilder;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-21T16:18:46+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.13 (Homebrew)"
)
@Component
public class CircleDtoMapperImpl implements CircleDtoMapper {

    @Override
    public UserResponseDto toUserResponseDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponseDtoBuilder userResponseDto = UserResponseDto.builder();

        userResponseDto.profileImageUrl( mapUuidFileToFileUrl( user.getUserProfileImage() ) );
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
        userResponseDto.rejectionOrDropReason( user.getRejectionOrDropReason() );
        userResponseDto.createdAt( user.getCreatedAt() );
        userResponseDto.updatedAt( user.getUpdatedAt() );
        userResponseDto.isV2( user.getIsV2() );

        return userResponseDto.build();
    }

    @Override
    public CircleResponseDto toCircleResponseDto(Circle circle) {
        if ( circle == null ) {
            return null;
        }

        CircleResponseDtoBuilder circleResponseDto = CircleResponseDto.builder();

        circleResponseDto.mainImage( mapUuidFileToFileUrl( circle.getCircleMainImage() ) );
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
            circleResponseDto.mainImage( mapUuidFileToFileUrl( circle.getCircleMainImage() ) );
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
            circlesResponseDto.mainImage( mapUuidFileToFileUrl( circle.getCircleMainImage() ) );
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
            circlesResponseDto.mainImage( mapUuidFileToFileUrl( circle.getCircleMainImage() ) );
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
            circleResponseDto.mainImage( mapUuidFileToFileUrl( circle.getCircleMainImage() ) );
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

    @Override
    public ExportCircleMemberToExcelResponseDto toExportCircleMemberToExcelResponseDto(User user, UserCouncilFee userCouncilFee, Integer restOfSemester, Boolean isAppliedThisSemester, Integer appliedSemester) {
        if ( user == null && userCouncilFee == null && restOfSemester == null && isAppliedThisSemester == null && appliedSemester == null ) {
            return null;
        }

        ExportCircleMemberToExcelResponseDtoBuilder exportCircleMemberToExcelResponseDto = ExportCircleMemberToExcelResponseDto.builder();

        if ( user != null ) {
            exportCircleMemberToExcelResponseDto.email( user.getEmail() );
            exportCircleMemberToExcelResponseDto.name( user.getName() );
            exportCircleMemberToExcelResponseDto.nickname( user.getNickname() );
            exportCircleMemberToExcelResponseDto.admissionYear( user.getAdmissionYear() );
            exportCircleMemberToExcelResponseDto.studentId( user.getStudentId() );
            exportCircleMemberToExcelResponseDto.major( user.getMajor() );
            exportCircleMemberToExcelResponseDto.phoneNumber( user.getPhoneNumber() );
            exportCircleMemberToExcelResponseDto.academicStatus( user.getAcademicStatus() );
            exportCircleMemberToExcelResponseDto.currentSemester( user.getCurrentCompletedSemester() );
            exportCircleMemberToExcelResponseDto.graduationYear( user.getGraduationYear() );
            exportCircleMemberToExcelResponseDto.graduationType( user.getGraduationType() );
            exportCircleMemberToExcelResponseDto.createdAt( user.getCreatedAt() );
        }
        if ( userCouncilFee != null ) {
            exportCircleMemberToExcelResponseDto.paidAt( userCouncilFee.getPaidAt() );
            exportCircleMemberToExcelResponseDto.paidSemester( userCouncilFee.getNumOfPaidSemester() );
            exportCircleMemberToExcelResponseDto.isRefunded( userCouncilFee.getIsRefunded() );
        }
        if ( restOfSemester != null ) {
            exportCircleMemberToExcelResponseDto.restOfSemester( restOfSemester );
        }
        if ( isAppliedThisSemester != null ) {
            exportCircleMemberToExcelResponseDto.isAppliedThisSemester( isAppliedThisSemester );
        }
        if ( appliedSemester != null ) {
            exportCircleMemberToExcelResponseDto.appliedSemester( appliedSemester );
        }

        return exportCircleMemberToExcelResponseDto.build();
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
