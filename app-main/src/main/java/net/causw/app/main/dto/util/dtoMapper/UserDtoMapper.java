package net.causw.app.main.dto.util.dtoMapper;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.user.UserAdmission;
import net.causw.app.main.domain.model.entity.user.UserAdmissionLog;
import net.causw.app.main.domain.model.entity.userInfo.UserCareer;
import net.causw.app.main.domain.model.entity.userInfo.UserInfo;
import net.causw.app.main.dto.comment.CommentsOfUserResponseDto;
import net.causw.app.main.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.app.main.dto.post.PostsResponseDto;
import net.causw.app.main.dto.user.UserAdmissionResponseDto;
import net.causw.app.main.dto.user.UserAdmissionsResponseDto;
import net.causw.app.main.dto.user.UserCommentsResponseDto;
import net.causw.app.main.dto.user.UserFcmTokenResponseDto;
import net.causw.app.main.dto.user.UserFindIdResponseDto;
import net.causw.app.main.dto.user.UserPostResponseDto;
import net.causw.app.main.dto.user.UserPostsResponseDto;
import net.causw.app.main.dto.user.UserPrivilegedResponseDto;
import net.causw.app.main.dto.user.UserResponseDto;
import net.causw.app.main.dto.user.UserSignInResponseDto;
import net.causw.app.main.dto.user.UserSignOutResponseDto;
import net.causw.app.main.dto.userInfo.UserCareerDto;
import net.causw.app.main.dto.userInfo.UserInfoResponseDto;
import net.causw.app.main.dto.userInfo.UserInfoSummaryResponseDto;
import net.causw.app.main.dto.util.dtoMapper.custom.UuidFileToUrlDtoMapper;
import net.causw.global.constant.StaticValue;

@Mapper(componentModel = "spring")
public interface UserDtoMapper extends UuidFileToUrlDtoMapper {

	UserDtoMapper INSTANCE = Mappers.getMapper(UserDtoMapper.class);

	@Mapping(target = "email", source = "user.email", qualifiedByName = "maskEmail")
	UserFindIdResponseDto toUserfindIdResponseDto(User user);

	@Mapping(target = "id", source = "user.id")
	@Mapping(target = "email", source = "user.email")
	@Mapping(target = "name", source = "user.name")
	@Mapping(target = "studentId", source = "user.studentId")
	@Mapping(target = "admissionYear", source = "user.admissionYear")
	@Mapping(target = "roles", source = "user.roles")
	@Mapping(target = "profileImageUrl", source = "user.userProfileImage", qualifiedByName = "mapUuidFileToFileUrl")
	@Mapping(target = "state", source = "user.state")
	@Mapping(target = "nickname", source = "user.nickname")
	@Mapping(target = "major", source = "user.major")
	@Mapping(target = "department", source = "user.department")
	@Mapping(target = "academicStatus", source = "user.academicStatus")
	@Mapping(target = "currentCompletedSemester", source = "user.currentCompletedSemester")
	@Mapping(target = "graduationYear", source = "user.graduationYear")
	@Mapping(target = "graduationType", source = "user.graduationType")
	@Mapping(target = "phoneNumber", source = "user.phoneNumber", qualifiedByName = "maskPhoneNumber")
	@Mapping(target = "rejectionOrDropReason", source = "user.rejectionOrDropReason")
	@Mapping(target = "createdAt", source = "user.createdAt")
	@Mapping(target = "updatedAt", source = "user.updatedAt")
	UserResponseDto toUserResponseDto(User user, List<String> circleIdIfLeader, List<String> circleNameIfLeader);
	// circleIdIfLeader, circleNameIfLeader는 경우에 따라 null을 할당합니다.(기존 UserResponseDto.from을 사용하는 경우)

	@Mapping(target = "id", source = "user.id")
	@Mapping(target = "email", source = "user.email")
	@Mapping(target = "name", source = "user.name")
	@Mapping(target = "studentId", source = "user.studentId")
	@Mapping(target = "admissionYear", source = "user.admissionYear")
	@Mapping(target = "roles", source = "user.roles")
	@Mapping(target = "profileImageUrl", source = "user.userProfileImage", qualifiedByName = "mapUuidFileToFileUrl")
	@Mapping(target = "state", source = "user.state")
	@Mapping(target = "nickname", source = "user.nickname")
	@Mapping(target = "major", source = "user.major")
	@Mapping(target = "department", source = "user.department")
	@Mapping(target = "academicStatus", source = "user.academicStatus")
	@Mapping(target = "currentCompletedSemester", source = "user.currentCompletedSemester")
	@Mapping(target = "graduationYear", source = "user.graduationYear")
	@Mapping(target = "graduationType", source = "user.graduationType")
	@Mapping(target = "phoneNumber", source = "user.phoneNumber", qualifiedByName = "maskPhoneNumber")
	@Mapping(target = "rejectionOrDropReason", source = "user.rejectionOrDropReason")
	@Mapping(target = "createdAt", source = "user.createdAt")
	@Mapping(target = "updatedAt", source = "user.updatedAt")
	@Mapping(target = "circleIdIfLeader", ignore = true)
	@Mapping(target = "circleNameIfLeader", ignore = true)
	UserResponseDto toUserResponseDto(User user);

	@Mapping(target = "id", source = "user.id")
	@Mapping(target = "email", source = "user.email")
	@Mapping(target = "name", source = "user.name")
	@Mapping(target = "studentId", source = "user.studentId")
	@Mapping(target = "admissionYear", source = "user.admissionYear")
	@Mapping(target = "profileImageUrl", source = "user.userProfileImage", qualifiedByName = "mapUuidFileToFileUrl")
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
	@Mapping(target = "profileImageUrl", source = "user.userProfileImage", qualifiedByName = "mapUuidFileToFileUrl")
	UserCommentsResponseDto toUserCommentsResponseDto(User user, Page<CommentsOfUserResponseDto> comment);

	@Mapping(target = "id", source = "comment.id")
	@Mapping(target = "content", source = "comment.content")
	@Mapping(target = "createdAt", source = "comment.createdAt")
	@Mapping(target = "updatedAt", source = "comment.updatedAt")
	@Mapping(target = "isDeleted", source = "comment.isDeleted")
	CommentsOfUserResponseDto toCommentsOfUserResponseDto(Comment comment, String boardId, String boardName,
		String postId, String postName, String circleId, String circleName);

	default UserPrivilegedResponseDto toUserPrivilegedResponseDto(
		List<UserResponseDto> president,
		List<UserResponseDto> vicePresident,
		List<UserResponseDto> council,
		List<UserResponseDto> leaderGrade1,
		List<UserResponseDto> leaderGrade2,
		List<UserResponseDto> leaderGrade3,
		List<UserResponseDto> leaderGrade4,
		List<UserResponseDto> leaderCircle,
		List<UserResponseDto> alumniLeader,
		List<UserResponseDto> alumniManager

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
			.leaderAlumni(alumniLeader)
			.alumniManager(alumniManager)
			.build();
	}

	UserSignInResponseDto toUserSignInResponseDto(String accessToken, String refreshToken);

	DuplicatedCheckResponseDto toDuplicatedCheckResponseDto(Boolean result);

	@Mapping(target = "id", source = "userAdmission.id")
	@Mapping(target = "user", source = "userAdmission.user")
	@Mapping(target = "attachImageUrlList", source = "userAdmission.userAdmissionAttachImageList", qualifiedByName = "mapUuidFileListToFileUrlList")
	@Mapping(target = "description", source = "userAdmission.description")
	@Mapping(target = "createdAt", source = "userAdmission.createdAt")
	@Mapping(target = "updatedAt", source = "userAdmission.updatedAt")
	UserAdmissionResponseDto toUserAdmissionResponseDto(UserAdmission userAdmission);

	@Mapping(target = "id", source = "userAdmission.id")
	@Mapping(target = "user", source = "user")
	@Mapping(target = "attachImageUrlList", source = "userAdmission.userAdmissionAttachImageList", qualifiedByName = "mapUuidFileListToFileUrlList")
	@Mapping(target = "description", source = "userAdmission.description")
	@Mapping(target = "createdAt", source = "userAdmission.createdAt")
	@Mapping(target = "updatedAt", source = "userAdmission.updatedAt")
	UserAdmissionResponseDto toUserAdmissionResponseDto(UserAdmission userAdmission, User user);

	@Mapping(target = "id", source = "userAdmissionLog.id")
	@Mapping(target = "user", source = "user")
	@Mapping(target = "description", source = "userAdmissionLog.description")
	@Mapping(target = "attachImageUrlList", source = "userAdmissionLog.userAdmissionLogAttachImageList", qualifiedByName = "mapUuidFileListToFileUrlList")
	@Mapping(target = "createdAt", source = "userAdmissionLog.createdAt")
	@Mapping(target = "updatedAt", source = "userAdmissionLog.updatedAt")
	@Mapping(target = "rejectReason", source = "userAdmissionLog.rejectReason")
	UserAdmissionResponseDto toUserAdmissionResponseDto(UserAdmissionLog userAdmissionLog, User user);

	@Mapping(target = "id", source = "userAdmission.id")
	@Mapping(target = "userName", source = "userAdmission.user.name")
	@Mapping(target = "userEmail", source = "userAdmission.user.email")
	@Mapping(target = "admissionYear", source = "userAdmission.user.admissionYear")
	@Mapping(target = "attachImageUrlList", source = "userAdmission.userAdmissionAttachImageList", qualifiedByName = "mapUuidFileListToFileUrlList")
	@Mapping(target = "description", source = "userAdmission.description")
	@Mapping(target = "userState", source = "userAdmission.user.state")
	@Mapping(target = "createdAt", source = "userAdmission.createdAt")
	@Mapping(target = "updatedAt", source = "userAdmission.updatedAt")
	@Mapping(target = "studentId", source = "userAdmission.user.id")
	UserAdmissionsResponseDto toUserAdmissionsResponseDto(UserAdmission userAdmission);

	UserSignOutResponseDto toUserSignOutResponseDto(String message);

	@Mapping(target = "fcmToken", source = "fcmTokens")
	UserFcmTokenResponseDto toUserFcmTokenResponseDto(User user);

	@Named("mapUserCareerListToResponseDtoList")
	default List<UserCareerDto> mapUserCareerListToResponseDtoList(List<UserCareer> userCareerList) {
		return userCareerList.stream().map(
			career -> UserCareerDto.builder()
				.id(career.getId())
				.startYear(career.getStartYear())
				.startMonth(career.getStartMonth())
				.endYear(career.getEndYear())
				.endMonth(career.getEndMonth())
				.description(career.getDescription())
				.build()
		).collect(Collectors.toList());
	}

	@Mapping(target = "id", source = "userInfo.id")
	@Mapping(target = "userId", source = "userInfo.user.id")
	@Mapping(target = "name", source = "userInfo.user.name")
	@Mapping(target = "email", source = "userInfo.user.email")
	@Mapping(target = "admissionYear", source = "userInfo.user.admissionYear")
	@Mapping(target = "profileImageUrl", source = "userInfo.user.userProfileImage", qualifiedByName = "mapUuidFileToFileUrl")
	@Mapping(target = "major", source = "userInfo.user.major")
	@Mapping(target = "department", source = "userInfo.user.department")
	@Mapping(target = "description", source = "userInfo.description")
	@Mapping(target = "job", source = "userInfo.job")
		// @Mapping(target = "userCareer", source = "userInfo.userCareer", qualifiedByName = "mapUserCareerListToResponseDtoList")
	UserInfoSummaryResponseDto toUserInfoSummaryResponseDto(UserInfo userInfo);

	@Mapping(target = "id", source = "userInfo.id")
	@Mapping(target = "userId", source = "userInfo.user.id")
	@Mapping(target = "name", source = "userInfo.user.name")
	@Mapping(target = "email", source = "userInfo.user.email")
	@Mapping(target = "phoneNumber", source = "userInfo.user.phoneNumber", qualifiedByName = "maskPhoneNumber")
	@Mapping(target = "admissionYear", source = "userInfo.user.admissionYear")
	@Mapping(target = "profileImageUrl", source = "userInfo.user.userProfileImage", qualifiedByName = "mapUuidFileToFileUrl")
	@Mapping(target = "major", source = "userInfo.user.major")
	@Mapping(target = "department", source = "userInfo.user.department")
	@Mapping(target = "roles", source = "userInfo.user.roles")
	@Mapping(target = "academicStatus", source = "userInfo.user.academicStatus")
	@Mapping(target = "description", source = "userInfo.description")
	@Mapping(target = "job", source = "userInfo.job")
	@Mapping(target = "userCareer", source = "userInfo.userCareer", qualifiedByName = "mapUserCareerListToResponseDtoList")
	@Mapping(target = "socialLinks", source = "userInfo.socialLinks")
	@Mapping(target = "isPhoneNumberVisible", source = "userInfo.isPhoneNumberVisible")
	UserInfoResponseDto toUserInfoResponseDto(UserInfo userInfo);

	//TODO : 운영계 반영 성공시 삭제
	@Named("maskPhoneNumber")
	default String maskPhoneNumber(String phoneNumber) {
		if (phoneNumber == null) {
			return null;
		}
		if (phoneNumber.startsWith(StaticValue.TEMP_PHONE_NUMBER_PREFIX)) {
			return StaticValue.NO_PHONE_NUMBER_MESSAGE;
		}
		return phoneNumber;
	}

	@Named("maskEmail")
	default String maskEmail(String email) {
		if (email == null || !email.contains("@")) {
			return email; // 잘못된 형식은 그냥 리턴
		}

		String[] parts = email.split("@", 2);
		String localPart = parts[0];
		String domainPart = parts[1];

		// 로컬 파트의 앞 2글자까지 보여주고 나머지는 마스킹
		int visibleCount = Math.min(2, localPart.length());
		String visible = localPart.substring(0, visibleCount);
		String masked = "*".repeat(Math.max(0, localPart.length() - visibleCount));

		return visible + masked + "@" + domainPart;
	}
}