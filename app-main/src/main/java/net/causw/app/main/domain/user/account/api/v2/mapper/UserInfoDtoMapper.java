package net.causw.app.main.domain.user.account.api.v2.mapper;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoDetailResponseDto;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoSummaryResponseDto;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.shared.dto.util.dtoMapper.custom.UuidFileToUrlDtoMapper;

@Mapper(componentModel = "spring")
public interface UserInfoDtoMapper extends UuidFileToUrlDtoMapper {

	// 동문 수첩 프로필 상세 조회
	@Mapping(target = "id", source = "id")
	@Mapping(target = "profileImageUrl", source = "user.userProfileImage", qualifiedByName = "mapUuidFileToFileUrl")
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

	// 내 동문 수첩 프로필 상세 조회
	@InheritConfiguration(name = "toUserInfoDetailResponseDto")
	@Mapping(target = "phoneNumber", source = "user.phoneNumber")
	@Mapping(target = "isPhoneNumberVisible", constant = "true")
	@Mapping(target = "isMessageVisible", constant = "true")
	UserInfoDetailResponseDto toMyUserInfoDetailResponseDto(UserInfo userInfo);

	// 동문 수첩 프로필 리스트 조회
	@Mapping(target = "id", source = "id")
	@Mapping(target = "profileImageUrl", source = "user.userProfileImage", qualifiedByName = "mapUuidFileToFileUrl")
	@Mapping(target = "name", source = "user.name")
	@Mapping(target = "admissionYear", source = ".", qualifiedByName = "mapAdmissionYear")
	@Mapping(target = "academicStatus", source = ".", qualifiedByName = "mapAcademicStatus")
	@Mapping(target = "job", source = "job")
	@Mapping(target = "description", source = "description")
	UserInfoSummaryResponseDto toUserInfoSummaryResponseDto(UserInfo userInfo);

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
			default -> "기타"; // 교수, 중퇴, 정학, 미정
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