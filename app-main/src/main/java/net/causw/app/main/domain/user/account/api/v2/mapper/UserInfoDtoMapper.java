package net.causw.app.main.domain.user.account.api.v2.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoDetailResponseDto;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.shared.dto.util.dtoMapper.custom.UuidFileToUrlDtoMapper;

@Mapper(componentModel = "spring")
public interface UserInfoDtoMapper extends UuidFileToUrlDtoMapper {

	@Mapping(target = "id", source = "id")
	@Mapping(target = "profileImageUrl", source = "userInfo.user.userProfileImage", qualifiedByName = "mapUuidFileToFileUrl")
	@Mapping(target = "name", source = "user.name")
	@Mapping(target = "admissionYear", source = ".", qualifiedByName = "mapAdmissionYear")
	@Mapping(target = "academicStatus", source = ".", qualifiedByName = "mapAcademicStatus")
	@Mapping(target = "job", source = "job")
	@Mapping(target = "description", source = "description")
	@Mapping(target = "email", source = "user.email")
	@Mapping(target = "phoneNumber", source = ".", qualifiedByName = "mapPhoneNumber")
	@Mapping(target = "isPhoneNumberVisible", source = "phoneNumberVisible")
	@Mapping(target = "isMessageVisible", source = "messageVisible")
	@Mapping(target = "socialLinks", source = "socialLinks")
	@Mapping(target = "techStack", source = "userTechStack")
	@Mapping(target = "userCareer", source = "userCareer")
	@Mapping(target = "userProject", source = "userProject")
	@Mapping(target = "userInterestTech", source = "userInterestTech")
	@Mapping(target = "userInterestDomain", source = "userInterestDomain")
	UserInfoDetailResponseDto toUserInfoDetailResponseDto(UserInfo userInfo);

	@Named("mapAdmissionYear")
	static String mapAdmissionYear(UserInfo userInfo) {
		return String.format("%s학번",
			userInfo.getUser().getAdmissionYear().toString().substring(2, 4));
	}

	@Named("mapAcademicStatus")
	static String mapAcademicStatus(UserInfo userInfo) {
		return switch (userInfo.getUser().getAcademicStatus()) {
			case ENROLLED -> "재학생";
			case LEAVE_OF_ABSENCE -> "휴학생";
			case GRADUATED -> "졸업생";
			case PROFESSOR -> "교수";
			default -> "기타"; // 중퇴, 정학, 미정
		};
	}

	@Named("mapPhoneNumber")
	static String mapPhoneNumber(UserInfo userInfo) {
		if (userInfo.isPhoneNumberVisible()) {
			return userInfo.getUser().getPhoneNumber();
		} else {
			return null;
		}
	}
}