package net.causw.application.dto.util;

import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.user.UserAdmission;
import net.causw.application.dto.comment.CommentsOfUserResponseDto;
import net.causw.application.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.application.dto.post.PostsResponseDto;
import net.causw.application.dto.user.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.LinkedList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserDtoMapper extends UuidFileToUrlDtoMapper {

    UserDtoMapper INSTANCE = Mappers.getMapper(UserDtoMapper.class);

    @Mapping(target = "email", source = "user.email")
    UserFindIdResponseDto toUserfindIdResponseDto(User user);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "studentId", source = "user.studentId")
    @Mapping(target = "admissionYear", source = "user.admissionYear")
    @Mapping(target = "roles", source = "user.roles")
    @Mapping(target = "profileImageUrl", source = "user.profileImageUuidFile", qualifiedByName = "mapUuidFileToFileUrl")
    @Mapping(target = "state", source = "user.state")
    @Mapping(target = "nickname", source = "user.nickname")
    @Mapping(target = "major", source = "user.major")
    @Mapping(target = "academicStatus", source = "user.academicStatus")
    @Mapping(target = "currentCompletedSemester", source = "user.currentCompletedSemester")
    @Mapping(target = "graduationYear", source = "user.graduationYear")
    @Mapping(target = "graduationType", source = "user.graduationType")
    @Mapping(target = "phoneNumber", source = "user.phoneNumber")
    UserResponseDto toUserResponseDto(User user, List<String> circleIdIfLeader, List<String> circleNameIfLeader);
    // circleIdIfLeader, circleNameIfLeader는 경우에 따라 null을 할당합니다.(기존 UserResponseDto.from을 사용하는 경우)

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "studentId", source = "user.studentId")
    @Mapping(target = "admissionYear", source = "user.admissionYear")
    @Mapping(target = "profileImageUrl", source = "user.profileImageUuidFile", qualifiedByName = "mapUuidFileToFileUrl")
    @Mapping(target = "posts", source = "post")
    UserPostsResponseDto toUserPostsResponseDto(User user, Page<PostsResponseDto> post);


    @Mapping(target = "id", source = "post.id")
    @Mapping(target = "title", source = "post.title")
    @Mapping(target = "boardId", source = "board.id")
    @Mapping(target = "boardName", source = "board.name")
    @Mapping(target = "circleId", source = "board.circle.id")
    @Mapping(target = "circleName", source = "board.circle.name")
    @Mapping(target = "createdAt", source = "post.createdAt")
    @Mapping(target = "updatedAt", source = "post.updatedAt")
    UserPostResponseDto toUserPostResponseDto(Post post, Board board, Long numComment);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "studentId", source = "user.studentId")
    @Mapping(target = "admissionYear", source = "user.admissionYear")
    @Mapping(target = "profileImageUrl", source = "user.profileImageUuidFile", qualifiedByName = "mapUuidFileToFileUrl")
    UserCommentsResponseDto toUserCommentsResponseDto(User user, Page<CommentsOfUserResponseDto> comment);

    @Mapping(target = "id", source = "comment.id")
    @Mapping(target = "content", source = "comment.content")
    @Mapping(target = "createdAt", source = "comment.createdAt")
    @Mapping(target = "updatedAt", source = "comment.updatedAt")
    @Mapping(target = "isDeleted", source = "comment.isDeleted")
    CommentsOfUserResponseDto toCommentsOfUserResponseDto(Comment comment, String boardId, String boardName, String postId, String postName, String circleId, String circleName);

    default UserPrivilegedResponseDto toUserPrivilegedResponseDto(
            List<UserResponseDto> president,
            List<UserResponseDto> vicePresident,
            List<UserResponseDto> council,
            List<UserResponseDto> leaderGrade1,
            List<UserResponseDto> leaderGrade2,
            List<UserResponseDto> leaderGrade3,
            List<UserResponseDto> leaderGrade4,
            List<UserResponseDto> leaderCircle,
            List<UserResponseDto> alumni
    ) {
        List<UserResponseDto> leaderGrade = new LinkedList<>(leaderGrade1);
        leaderGrade.addAll(leaderGrade2);
        leaderGrade.addAll(leaderGrade3);
        leaderGrade.addAll(leaderGrade4);
        return UserPrivilegedResponseDto.builder()
                .presidentUser(president)
                .vicePresidentUser(vicePresident)
                .councilUsers(council)
                .leaderGradeUsers(leaderGrade)
                .leaderCircleUsers(leaderCircle)
                .leaderAlumni(alumni)
                .build();
    }

    UserSignInResponseDto toUserSignInResponseDto(String accessToken, String refreshToken);

    DuplicatedCheckResponseDto toDuplicatedCheckResponseDto(Boolean result);

    @Mapping(target = "id", source = "userAdmission.id")
    @Mapping(target = "user", source = "userAdmission.user")
    @Mapping(target = "attachImageUrlList", source = "userAdmission.attachImageUuidFileList", qualifiedByName = "mapUuidFileListToFileUrlList")
    @Mapping(target = "description", source = "userAdmission.description")
    @Mapping(target = "createdAt", source = "userAdmission.createdAt")
    @Mapping(target = "updatedAt", source = "userAdmission.updatedAt")
    UserAdmissionResponseDto toUserAdmissionResponseDto(UserAdmission userAdmission);

    @Mapping(target = "id", source = "userAdmission.id")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "attachImageUrlList", source = "userAdmission.attachImageUuidFileList", qualifiedByName = "mapUuidFileListToFileUrlList")
    @Mapping(target = "description", source = "userAdmission.description")
    @Mapping(target = "createdAt", source = "userAdmission.createdAt")
    @Mapping(target = "updatedAt", source = "userAdmission.updatedAt")
    UserAdmissionResponseDto toUserAdmissionResponseDto(UserAdmission userAdmission, User user);

    @Mapping(target = "id", source = "userAdmission.id")
    @Mapping(target = "userName", source = "userAdmission.user.name")
    @Mapping(target = "userEmail", source = "userAdmission.user.email")
    @Mapping(target = "admissionYear", source = "userAdmission.user.admissionYear")
    @Mapping(target = "attachImageUrlList", source = "userAdmission.attachImageUuidFileList", qualifiedByName = "mapUuidFileListToFileUrlList")
    @Mapping(target = "description", source = "userAdmission.description")
    @Mapping(target = "userState", source = "userAdmission.user.state")
    @Mapping(target = "createdAt", source = "userAdmission.createdAt")
    @Mapping(target = "updatedAt", source = "userAdmission.updatedAt")
    UserAdmissionsResponseDto toUserAdmissionsResponseDto(UserAdmission userAdmission);

    UserSignOutResponseDto toUserSignOutResponseDto(String message);

}
