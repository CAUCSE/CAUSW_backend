package net.causw.app.main.domain.user.account.service.mapper;

import java.util.List;
import java.util.Set;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.service.dto.result.UserInfoDetailResult;
import net.causw.app.main.domain.user.account.service.dto.result.UserInfoSummaryResult;
import net.causw.app.main.shared.dto.util.dtoMapper.custom.UuidFileToUrlDtoMapper;

@Mapper(componentModel = "spring")
public interface UserInfoMapper extends UuidFileToUrlDtoMapper {

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
	@Mapping(target = "socialLinks", source = "socialLinks")
	@Mapping(target = "techStack", source = "userTechStack", qualifiedByName = "sortStringsAsc")
	@Mapping(target = "userCareer", source = "userCareer")
	@Mapping(target = "userProject", source = "userProject")
	@Mapping(target = "userInterestTech", source = "userInterestTech", qualifiedByName = "sortStringsAsc")
	@Mapping(target = "userInterestDomain", source = "userInterestDomain", qualifiedByName = "sortStringsAsc")
	UserInfoDetailResult toUserInfoDetailResult(UserInfo userInfo);

	// 내 동문 수첩 프로필 상세 조회
	@InheritConfiguration(name = "toUserInfoDetailResult")
	@Mapping(target = "phoneNumber", source = "user.phoneNumber")
	@Mapping(target = "isPhoneNumberVisible", constant = "true")
	UserInfoDetailResult toMyUserInfoDetailResult(UserInfo userInfo);

	// 동문 수첩 프로필 리스트 조회
	@Mapping(target = "id", source = "id")
	@Mapping(target = "profileImageUrl", source = "user.userProfileImage", qualifiedByName = "mapUuidFileToFileUrl")
	@Mapping(target = "name", source = "user.name")
	@Mapping(target = "admissionYear", source = ".", qualifiedByName = "mapAdmissionYear")
	@Mapping(target = "academicStatus", source = ".", qualifiedByName = "mapAcademicStatus")
	@Mapping(target = "job", source = "job")
	@Mapping(target = "description", source = "description")
	UserInfoSummaryResult toUserInfoSummaryResult(UserInfo userInfo);

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

	@Named("sortStringsAsc")
	static List<String> sortStringsAsc(Set<String> set) {
		if (set == null || set.isEmpty())
			return List.of();
		return set.stream()
			.filter(s -> s != null && !s.isBlank())
			.sorted(String.CASE_INSENSITIVE_ORDER)
			.toList();
	}
}
