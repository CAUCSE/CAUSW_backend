package net.causw.app.main.dto.util.dtoMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.circle.Circle;
import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.user.UserAdmission;
import net.causw.app.main.domain.model.entity.user.UserAdmissionLog;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.domain.model.enums.user.UserState;
import net.causw.app.main.dto.comment.CommentsOfUserResponseDto;
import net.causw.app.main.dto.comment.CommentsOfUserResponseDto.CommentsOfUserResponseDtoBuilder;
import net.causw.app.main.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.app.main.dto.duplicate.DuplicatedCheckResponseDto.DuplicatedCheckResponseDtoBuilder;
import net.causw.app.main.dto.post.PostsResponseDto;
import net.causw.app.main.dto.user.UserAdmissionResponseDto;
import net.causw.app.main.dto.user.UserAdmissionResponseDto.UserAdmissionResponseDtoBuilder;
import net.causw.app.main.dto.user.UserAdmissionsResponseDto;
import net.causw.app.main.dto.user.UserAdmissionsResponseDto.UserAdmissionsResponseDtoBuilder;
import net.causw.app.main.dto.user.UserCommentsResponseDto;
import net.causw.app.main.dto.user.UserCommentsResponseDto.UserCommentsResponseDtoBuilder;
import net.causw.app.main.dto.user.UserFcmTokenResponseDto;
import net.causw.app.main.dto.user.UserFcmTokenResponseDto.UserFcmTokenResponseDtoBuilder;
import net.causw.app.main.dto.user.UserFindIdResponseDto;
import net.causw.app.main.dto.user.UserFindIdResponseDto.UserFindIdResponseDtoBuilder;
import net.causw.app.main.dto.user.UserPostResponseDto;
import net.causw.app.main.dto.user.UserPostResponseDto.UserPostResponseDtoBuilder;
import net.causw.app.main.dto.user.UserPostsResponseDto;
import net.causw.app.main.dto.user.UserPostsResponseDto.UserPostsResponseDtoBuilder;
import net.causw.app.main.dto.user.UserResponseDto;
import net.causw.app.main.dto.user.UserResponseDto.UserResponseDtoBuilder;
import net.causw.app.main.dto.user.UserSignInResponseDto;
import net.causw.app.main.dto.user.UserSignInResponseDto.UserSignInResponseDtoBuilder;
import net.causw.app.main.dto.user.UserSignOutResponseDto;
import net.causw.app.main.dto.user.UserSignOutResponseDto.UserSignOutResponseDtoBuilder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-21T16:18:46+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.13 (Homebrew)"
)
@Component
public class UserDtoMapperImpl implements UserDtoMapper {

    @Override
    public UserFindIdResponseDto toUserfindIdResponseDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserFindIdResponseDtoBuilder userFindIdResponseDto = UserFindIdResponseDto.builder();

        userFindIdResponseDto.email( user.getEmail() );

        return userFindIdResponseDto.build();
    }

    @Override
    public UserResponseDto toUserResponseDto(User user, List<String> circleIdIfLeader, List<String> circleNameIfLeader) {
        if ( user == null && circleIdIfLeader == null && circleNameIfLeader == null ) {
            return null;
        }

        UserResponseDtoBuilder userResponseDto = UserResponseDto.builder();

        if ( user != null ) {
            userResponseDto.id( user.getId() );
            userResponseDto.email( user.getEmail() );
            userResponseDto.name( user.getName() );
            userResponseDto.studentId( user.getStudentId() );
            userResponseDto.admissionYear( user.getAdmissionYear() );
            Set<Role> set = user.getRoles();
            if ( set != null ) {
                userResponseDto.roles( new HashSet<Role>( set ) );
            }
            userResponseDto.profileImageUrl( mapUuidFileToFileUrl( user.getUserProfileImage() ) );
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
        }
        if ( circleIdIfLeader != null ) {
            List<String> list = circleIdIfLeader;
            if ( list != null ) {
                userResponseDto.circleIdIfLeader( new ArrayList<String>( list ) );
            }
        }
        if ( circleNameIfLeader != null ) {
            List<String> list1 = circleNameIfLeader;
            if ( list1 != null ) {
                userResponseDto.circleNameIfLeader( new ArrayList<String>( list1 ) );
            }
        }

        return userResponseDto.build();
    }

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
        userResponseDto.profileImageUrl( mapUuidFileToFileUrl( user.getUserProfileImage() ) );
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
    public UserPostsResponseDto toUserPostsResponseDto(User user, Page<PostsResponseDto> post) {
        if ( user == null && post == null ) {
            return null;
        }

        UserPostsResponseDtoBuilder userPostsResponseDto = UserPostsResponseDto.builder();

        if ( user != null ) {
            userPostsResponseDto.id( user.getId() );
            userPostsResponseDto.email( user.getEmail() );
            userPostsResponseDto.name( user.getName() );
            userPostsResponseDto.studentId( user.getStudentId() );
            userPostsResponseDto.admissionYear( user.getAdmissionYear() );
            userPostsResponseDto.profileImageUrl( mapUuidFileToFileUrl( user.getUserProfileImage() ) );
        }
        if ( post != null ) {
            userPostsResponseDto.posts( post );
        }

        return userPostsResponseDto.build();
    }

    @Override
    public UserPostResponseDto toUserPostResponseDto(Post post, Board board, Long numComment) {
        if ( post == null && board == null && numComment == null ) {
            return null;
        }

        UserPostResponseDtoBuilder userPostResponseDto = UserPostResponseDto.builder();

        if ( post != null ) {
            userPostResponseDto.id( post.getId() );
            userPostResponseDto.title( post.getTitle() );
            userPostResponseDto.createdAt( post.getCreatedAt() );
            userPostResponseDto.updatedAt( post.getUpdatedAt() );
        }
        if ( board != null ) {
            userPostResponseDto.boardId( board.getId() );
            userPostResponseDto.boardName( board.getName() );
            userPostResponseDto.circleId( boardCircleId( board ) );
            userPostResponseDto.circleName( boardCircleName( board ) );
        }
        if ( numComment != null ) {
            userPostResponseDto.numComment( numComment );
        }

        return userPostResponseDto.build();
    }

    @Override
    public UserCommentsResponseDto toUserCommentsResponseDto(User user, Page<CommentsOfUserResponseDto> comment) {
        if ( user == null && comment == null ) {
            return null;
        }

        UserCommentsResponseDtoBuilder userCommentsResponseDto = UserCommentsResponseDto.builder();

        if ( user != null ) {
            userCommentsResponseDto.id( user.getId() );
            userCommentsResponseDto.email( user.getEmail() );
            userCommentsResponseDto.name( user.getName() );
            userCommentsResponseDto.studentId( user.getStudentId() );
            userCommentsResponseDto.admissionYear( user.getAdmissionYear() );
            userCommentsResponseDto.profileImageUrl( mapUuidFileToFileUrl( user.getUserProfileImage() ) );
        }
        if ( comment != null ) {
            userCommentsResponseDto.comment( comment );
        }

        return userCommentsResponseDto.build();
    }

    @Override
    public CommentsOfUserResponseDto toCommentsOfUserResponseDto(Comment comment, String boardId, String boardName, String postId, String postName, String circleId, String circleName) {
        if ( comment == null && boardId == null && boardName == null && postId == null && postName == null && circleId == null && circleName == null ) {
            return null;
        }

        CommentsOfUserResponseDtoBuilder commentsOfUserResponseDto = CommentsOfUserResponseDto.builder();

        if ( comment != null ) {
            commentsOfUserResponseDto.id( comment.getId() );
            commentsOfUserResponseDto.content( comment.getContent() );
            commentsOfUserResponseDto.createdAt( comment.getCreatedAt() );
            commentsOfUserResponseDto.updatedAt( comment.getUpdatedAt() );
            commentsOfUserResponseDto.isDeleted( comment.getIsDeleted() );
        }
        if ( boardId != null ) {
            commentsOfUserResponseDto.boardId( boardId );
        }
        if ( boardName != null ) {
            commentsOfUserResponseDto.boardName( boardName );
        }
        if ( postId != null ) {
            commentsOfUserResponseDto.postId( postId );
        }
        if ( postName != null ) {
            commentsOfUserResponseDto.postName( postName );
        }
        if ( circleId != null ) {
            commentsOfUserResponseDto.circleId( circleId );
        }
        if ( circleName != null ) {
            commentsOfUserResponseDto.circleName( circleName );
        }

        return commentsOfUserResponseDto.build();
    }

    @Override
    public UserSignInResponseDto toUserSignInResponseDto(String accessToken, String refreshToken) {
        if ( accessToken == null && refreshToken == null ) {
            return null;
        }

        UserSignInResponseDtoBuilder userSignInResponseDto = UserSignInResponseDto.builder();

        if ( accessToken != null ) {
            userSignInResponseDto.accessToken( accessToken );
        }
        if ( refreshToken != null ) {
            userSignInResponseDto.refreshToken( refreshToken );
        }

        return userSignInResponseDto.build();
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
    public UserAdmissionResponseDto toUserAdmissionResponseDto(UserAdmission userAdmission) {
        if ( userAdmission == null ) {
            return null;
        }

        UserAdmissionResponseDtoBuilder userAdmissionResponseDto = UserAdmissionResponseDto.builder();

        userAdmissionResponseDto.id( userAdmission.getId() );
        userAdmissionResponseDto.user( toUserResponseDto( userAdmission.getUser() ) );
        userAdmissionResponseDto.attachImageUrlList( mapUuidFileListToFileUrlList( userAdmission.getUserAdmissionAttachImageList() ) );
        userAdmissionResponseDto.description( userAdmission.getDescription() );
        userAdmissionResponseDto.createdAt( userAdmission.getCreatedAt() );
        userAdmissionResponseDto.updatedAt( userAdmission.getUpdatedAt() );

        return userAdmissionResponseDto.build();
    }

    @Override
    public UserAdmissionResponseDto toUserAdmissionResponseDto(UserAdmission userAdmission, User user) {
        if ( userAdmission == null && user == null ) {
            return null;
        }

        UserAdmissionResponseDtoBuilder userAdmissionResponseDto = UserAdmissionResponseDto.builder();

        if ( userAdmission != null ) {
            userAdmissionResponseDto.id( userAdmission.getId() );
            userAdmissionResponseDto.attachImageUrlList( mapUuidFileListToFileUrlList( userAdmission.getUserAdmissionAttachImageList() ) );
            userAdmissionResponseDto.description( userAdmission.getDescription() );
            userAdmissionResponseDto.createdAt( userAdmission.getCreatedAt() );
            userAdmissionResponseDto.updatedAt( userAdmission.getUpdatedAt() );
        }
        if ( user != null ) {
            userAdmissionResponseDto.user( toUserResponseDto( user ) );
        }

        return userAdmissionResponseDto.build();
    }

    @Override
    public UserAdmissionResponseDto toUserAdmissionResponseDto(UserAdmissionLog userAdmissionLog, User user) {
        if ( userAdmissionLog == null && user == null ) {
            return null;
        }

        UserAdmissionResponseDtoBuilder userAdmissionResponseDto = UserAdmissionResponseDto.builder();

        if ( userAdmissionLog != null ) {
            userAdmissionResponseDto.id( userAdmissionLog.getId() );
            userAdmissionResponseDto.description( userAdmissionLog.getDescription() );
            userAdmissionResponseDto.attachImageUrlList( mapUuidFileListToFileUrlList( userAdmissionLog.getUserAdmissionLogAttachImageList() ) );
            userAdmissionResponseDto.createdAt( userAdmissionLog.getCreatedAt() );
            userAdmissionResponseDto.updatedAt( userAdmissionLog.getUpdatedAt() );
            userAdmissionResponseDto.rejectReason( userAdmissionLog.getRejectReason() );
        }
        if ( user != null ) {
            userAdmissionResponseDto.user( toUserResponseDto( user ) );
        }

        return userAdmissionResponseDto.build();
    }

    @Override
    public UserAdmissionsResponseDto toUserAdmissionsResponseDto(UserAdmission userAdmission) {
        if ( userAdmission == null ) {
            return null;
        }

        UserAdmissionsResponseDtoBuilder userAdmissionsResponseDto = UserAdmissionsResponseDto.builder();

        userAdmissionsResponseDto.id( userAdmission.getId() );
        userAdmissionsResponseDto.userName( userAdmissionUserName( userAdmission ) );
        userAdmissionsResponseDto.userEmail( userAdmissionUserEmail( userAdmission ) );
        userAdmissionsResponseDto.admissionYear( userAdmissionUserAdmissionYear( userAdmission ) );
        userAdmissionsResponseDto.attachImageUrlList( mapUuidFileListToFileUrlList( userAdmission.getUserAdmissionAttachImageList() ) );
        userAdmissionsResponseDto.description( userAdmission.getDescription() );
        userAdmissionsResponseDto.userState( userAdmissionUserState( userAdmission ) );
        userAdmissionsResponseDto.createdAt( userAdmission.getCreatedAt() );
        userAdmissionsResponseDto.updatedAt( userAdmission.getUpdatedAt() );
        userAdmissionsResponseDto.studentId( userAdmissionUserId( userAdmission ) );

        return userAdmissionsResponseDto.build();
    }

    @Override
    public UserSignOutResponseDto toUserSignOutResponseDto(String message) {
        if ( message == null ) {
            return null;
        }

        UserSignOutResponseDtoBuilder userSignOutResponseDto = UserSignOutResponseDto.builder();

        userSignOutResponseDto.message( message );

        return userSignOutResponseDto.build();
    }

    @Override
    public UserFcmTokenResponseDto toUserFcmTokenResponseDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserFcmTokenResponseDtoBuilder userFcmTokenResponseDto = UserFcmTokenResponseDto.builder();

        Set<String> set = user.getFcmTokens();
        if ( set != null ) {
            userFcmTokenResponseDto.fcmToken( new ArrayList<String>( set ) );
        }

        return userFcmTokenResponseDto.build();
    }

    private String boardCircleId(Board board) {
        if ( board == null ) {
            return null;
        }
        Circle circle = board.getCircle();
        if ( circle == null ) {
            return null;
        }
        String id = circle.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String boardCircleName(Board board) {
        if ( board == null ) {
            return null;
        }
        Circle circle = board.getCircle();
        if ( circle == null ) {
            return null;
        }
        String name = circle.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String userAdmissionUserName(UserAdmission userAdmission) {
        if ( userAdmission == null ) {
            return null;
        }
        User user = userAdmission.getUser();
        if ( user == null ) {
            return null;
        }
        String name = user.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String userAdmissionUserEmail(UserAdmission userAdmission) {
        if ( userAdmission == null ) {
            return null;
        }
        User user = userAdmission.getUser();
        if ( user == null ) {
            return null;
        }
        String email = user.getEmail();
        if ( email == null ) {
            return null;
        }
        return email;
    }

    private Integer userAdmissionUserAdmissionYear(UserAdmission userAdmission) {
        if ( userAdmission == null ) {
            return null;
        }
        User user = userAdmission.getUser();
        if ( user == null ) {
            return null;
        }
        Integer admissionYear = user.getAdmissionYear();
        if ( admissionYear == null ) {
            return null;
        }
        return admissionYear;
    }

    private UserState userAdmissionUserState(UserAdmission userAdmission) {
        if ( userAdmission == null ) {
            return null;
        }
        User user = userAdmission.getUser();
        if ( user == null ) {
            return null;
        }
        UserState state = user.getState();
        if ( state == null ) {
            return null;
        }
        return state;
    }

    private String userAdmissionUserId(UserAdmission userAdmission) {
        if ( userAdmission == null ) {
            return null;
        }
        User user = userAdmission.getUser();
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
