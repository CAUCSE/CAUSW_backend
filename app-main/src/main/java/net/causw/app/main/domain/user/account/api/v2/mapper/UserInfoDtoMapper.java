package net.causw.app.main.domain.user.account.api.v2.mapper;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import net.causw.app.main.domain.user.account.api.v1.dto.UserInfoSummaryResponseDto;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserInfoListRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserInfoUpdateRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoDetailResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoSummaryResponse;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoListCondition;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoUpdateCommand;
import net.causw.app.main.domain.user.account.service.dto.result.UserInfoDetailResult;
import net.causw.app.main.domain.user.account.service.dto.result.UserInfoSummaryResult;
import net.causw.app.main.shared.dto.ProfileImageDto;
import net.causw.app.main.shared.dto.util.dtoMapper.custom.UuidFileToUrlDtoMapper;

@Mapper(componentModel = "spring")
public interface UserInfoDtoMapper extends UuidFileToUrlDtoMapper {

	// 동문 수첩 프로필 상세 조회
	@Mapping(target = "id", source = "id")
	@Mapping(target = "profileImage", source = ".", qualifiedByName = "mapProfileImage")
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
	UserInfoDetailResponse toUserInfoDetailResponseDto(UserInfo userInfo);

	// 내 동문 수첩 프로필 상세 조회
	@InheritConfiguration(name = "toUserInfoDetailResponseDto")
	@Mapping(target = "phoneNumber", source = "user.phoneNumber")
	@Mapping(target = "isPhoneNumberVisible", constant = "true")
	UserInfoDetailResponse toMyUserInfoDetailResponseDto(UserInfo userInfo);

	// 동문 수첩 프로필 리스트 조회
	@Mapping(target = "id", source = "id")
	@Mapping(target = "profileImage", source = ".", qualifiedByName = "mapProfileImage")
	@Mapping(target = "name", source = "user.name")
	@Mapping(target = "admissionYear", source = ".", qualifiedByName = "mapAdmissionYear")
	@Mapping(target = "academicStatus", source = ".", qualifiedByName = "mapAcademicStatus")
	@Mapping(target = "job", source = "job")
	@Mapping(target = "description", source = "description")
	UserInfoSummaryResponseDto toUserInfoSummaryResponseDto(UserInfo userInfo);

	UserInfoUpdateCommand toUpdateCommand(UserInfoUpdateRequest request);

	@Named("mapProfileImage")
	static ProfileImageDto mapProfileImage(UserInfo userInfo) {
		return ProfileImageDto.from(userInfo.getUser());
	}

	@Named("mapAdmissionYear")
	static String mapAdmissionYear(UserInfo userInfo) {
		return String.format("%s학번",
			userInfo.getUser().getAdmissionYear().toString().substring(2, 4));
	}

	UserInfoDetailResponse toDetailResponse(UserInfoDetailResult result);

	UserInfoSummaryResponse toSummaryResponse(UserInfoSummaryResult result);

	UserInfoListCondition toListCondition(UserInfoListRequest request);
}