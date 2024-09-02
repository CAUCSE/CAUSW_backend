package net.causw.application.dto.util;

import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.comment.ChildComment;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.user.UserAdmission;
import net.causw.application.dto.board.BoardMainResponseDto;
import net.causw.application.dto.board.BoardOfCircleResponseDto;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.circle.CircleResponseDto;
import net.causw.application.dto.comment.ChildCommentResponseDto;
import net.causw.application.dto.comment.CommentResponseDto;
import net.causw.application.dto.comment.CommentsOfUserResponseDto;
import net.causw.application.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.application.dto.file.FileResponseDto;
import net.causw.application.dto.post.BoardPostsResponseDto;
import net.causw.application.dto.post.PostContentDto;
import net.causw.application.dto.post.PostResponseDto;
import net.causw.application.dto.post.PostsResponseDto;
import net.causw.application.dto.user.*;
import net.causw.domain.model.enums.Role;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Custom Annotation을 사용하여 중복되는 @Mapping을 줄일 수 있습니다.
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
@Mapping(target = "writerName", source = "entity.writer.name")
@Mapping(target = "writerAdmissionYear", source = "entity.writer.admissionYear")
@Mapping(target = "writerProfileImages", source = "entity.writer.profileImages")
@interface CommonWriterMappings {}

@Mapper(componentModel = "spring")
public interface DtoMapper{

    DtoMapper INSTANCE = Mappers.getMapper(DtoMapper.class);

    // 자료형 변환 등이 필요하다면 아래 형식으로 메서드를 작성합니다.
    // 이 메서드는 post.attachment를 attachmentsToStringList 메서드로 List<FileResponseDto>로 변환합니다.
    // 메서드 수가 많아지면 별도의 Converter 클래스를 만들어 상속받는 식으로 처리해도 좋습니다.
    @Named("attachmentsToStringList")
    default List<FileResponseDto> attachmentsToStringList(String attachments) {
        if(attachments == null || attachments.isEmpty()) return List.of();
        return Arrays.stream(attachments.split(":::"))
                .map(FileResponseDto::from)
                .collect(Collectors.toList());
    }

    // Dto writerName 필드에 post.writer.name을 삽입한다는 의미입니다.
    @Mapping(target = "writerName", source = "entity.writer.name")
    @Mapping(target = "writerAdmissionYear", source = "entity.writer.admissionYear")
    @Mapping(target = "isAnonymous", source = "entity.isAnonymous")
    @Mapping(target = "isQuestion", source = "entity.isQuestion")
    @Mapping(target = "numLike", source = "numPostLike")
    @Mapping(target = "numFavorite", source = "numPostFavorite")
    PostsResponseDto toPostsResponseDto(Post entity, Long numComment, Long numPostLike, Long numPostFavorite);

    @CommonWriterMappings
    @Mapping(target = "boardName", source = "entity.board.name")
    @Mapping(target = "attachmentList", source = "entity.attachments", qualifiedByName = "attachmentsToStringList")
    @Mapping(target = "isAnonymous", source = "entity.isAnonymous")
    @Mapping(target = "isQuestion", source = "entity.isQuestion")
    @Mapping(target = "numLike", source = "numPostLike")
    @Mapping(target = "numFavorite", source = "numPostFavorite")
    PostResponseDto toPostResponseDto(Post entity, Long numPostLike, Long numPostFavorite,  Boolean updatable, Boolean deletable);

    @CommonWriterMappings
    @Mapping(target = "boardName", source = "entity.board.name")
    @Mapping(target = "attachmentList", source = "entity.attachments", qualifiedByName = "attachmentsToStringList")
    @Mapping(target = "content", source = "entity.content")
    @Mapping(target = "isAnonymous", source = "entity.isAnonymous")
    @Mapping(target = "isQuestion", source = "entity.isQuestion")
    @Mapping(target = "numLike", source = "numPostLike")
    @Mapping(target = "numFavorite", source = "numPostFavorite")
    PostResponseDto toPostResponseDtoExtended(Post entity, Page<CommentResponseDto> commentList, Long numComment, Long numPostLike, Long numPostFavorite, Boolean updatable, Boolean deletable);

    @Mapping(target = "title", source = "post.title")
    @Mapping(target = "contentId", source = "post.id")
    PostContentDto toPostContentDto(Post post);

    @CommonWriterMappings
    @Mapping(target = "postId", source = "entity.post.id")
    @Mapping(target = "isAnonymous", source = "entity.isAnonymous")
    @Mapping(target ="numLike", source = "numCommentLike")
    CommentResponseDto toCommentResponseDto(Comment entity, Long numChildComment, Long numCommentLike, List<ChildCommentResponseDto> childCommentList, Boolean updatable, Boolean deletable);

    @CommonWriterMappings
    @Mapping(target = "isAnonymous", source = "entity.isAnonymous")
    @Mapping(target ="numLike", source = "numChildCommentLike")
    ChildCommentResponseDto toChildCommentResponseDto(ChildComment entity, Long numChildCommentLike,  Boolean updatable, Boolean deletable);

    @Mapping(target = "boardId", source = "entity.id")
    @Mapping(target = "boardName", source = "entity.name")
    BoardPostsResponseDto toBoardPostsResponseDto(Board entity, Set<Role> userRole, Boolean writable, Boolean isFavorite, Page<PostsResponseDto> post);

    /** TODO: 각자 역할분담한 부분의 Dto를 위를 참고하여 아래 작성하시면 됩니다.
     *  기존에 Dto에 존재하던 of 메서드를 DtoMapper.INSTANCE.toDtoName(entity)로 대체하시면 됩니다.
     *  컴파일 후 DtoMapperImpl 파일을 확인하여 필드별로 제대로 매핑이 되었는지 확인해야 합니다.
     */

    // User
    @Mapping(target = "email", source = "entity.email")
    UserFindIdResponseDto toUserfindIdResponseDto(User entity);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "studentId", source = "user.studentId")
    @Mapping(target = "admissionYear", source = "user.admissionYear")
    @Mapping(target = "roles", source = "user.roles")
    @Mapping(target = "profileImages", source = "user.profileImages")
    @Mapping(target = "state", source = "user.state")
    @Mapping(target = "nickname", source = "user.nickname")
    @Mapping(target = "major", source = "user.major")
    @Mapping(target = "academicStatus", source = "user.academicStatus")
    @Mapping(target = "currentCompletedSemester", source = "user.currentCompletedSemester")
    @Mapping(target = "graduationYear", source = "user.graduationYear")
    @Mapping(target = "graduationMonth", source = "user.graduationMonth")
    @Mapping(target = "phoneNumber", source = "user.phoneNumber")
    UserResponseDto toUserResponseDto(User user, List<String> circleIdIfLeader, List<String> circleNameIfLeader);
    // circleIdIfLeader, circleNameIfLeader는 경우에 따라 null을 할당합니다.(기존 UserResponseDto.from을 사용하는 경우)

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "studentId", source = "user.studentId")
    @Mapping(target = "admissionYear", source = "user.admissionYear")
    @Mapping(target = "profileImages", source = "user.profileImages")
    UserPostsResponseDto toUserPostsResponseDto(User user, Page<UserPostResponseDto> post);

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
    @Mapping(target = "profileImages", source = "user.profileImages")
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

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "user", source = "entity.user")
    @Mapping(target = "attachImage", source = "entity.attachImage")
    @Mapping(target = "description", source = "entity.description")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    @Mapping(target = "updatedAt", source = "entity.updatedAt")
    UserAdmissionResponseDto toUserAdmissionResponseDto(UserAdmission entity);

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "attachImage", source = "entity.attachImage")
    @Mapping(target = "description", source = "entity.description")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    @Mapping(target = "updatedAt", source = "entity.updatedAt")
    UserAdmissionResponseDto toUserAdmissionResponseDto(UserAdmission entity, User user);

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "userName", source = "entity.user.name")
    @Mapping(target = "userEmail", source = "entity.user.email")
    @Mapping(target = "admissionYear", source = "entity.user.admissionYear")
    @Mapping(target = "attachImage", source = "entity.attachImage")
    @Mapping(target = "description", source = "entity.description")
    @Mapping(target = "userState", source = "entity.user.state")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    @Mapping(target = "updatedAt", source = "entity.updatedAt")
    UserAdmissionsResponseDto toUserAdmissionsResponseDto(UserAdmission entity);

    UserSignOutResponseDto toUserSignOutResponseDto(String message);

    // Board
    BoardResponseDto toBoardResponseDto(Board entity, List<String> createRoleList, Boolean writable, String circleId, String circleName);

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

    // Circle
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "name", source = "entity.name")
    @Mapping(target = "description", source = "entity.description")
    @Mapping(target = "isDeleted", source = "entity.isDeleted")
    @Mapping(target = "leaderId", source = "leader.id")
    @Mapping(target = "leaderName", source = "leader.name")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    CircleResponseDto toCircleResponseDto(Circle entity, User leader);

    // Locker


}
