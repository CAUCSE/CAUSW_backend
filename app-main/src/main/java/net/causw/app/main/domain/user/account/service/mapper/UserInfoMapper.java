package net.causw.app.main.domain.user.account.service.mapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.user.account.entity.userInfo.UserCareer;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.entity.userInfo.UserProject;
import net.causw.app.main.domain.user.account.service.dto.result.UserCareerResult;
import net.causw.app.main.domain.user.account.service.dto.result.UserInfoDetailResult;
import net.causw.app.main.domain.user.account.service.dto.result.UserInfoSummaryResult;
import net.causw.app.main.domain.user.account.service.dto.result.UserProjectResult;
import net.causw.app.main.shared.dto.util.dtoMapper.custom.UuidFileToUrlDtoMapper;

@Mapper(componentModel = "spring")
public interface UserInfoMapper extends UuidFileToUrlDtoMapper {

	// 동문 수첩 프로필 상세 조회
	@Mapping(target = "id", source = "userInfo.id")
	@Mapping(target = "profileImage", expression = "java(net.causw.app.main.shared.dto.ProfileImageDto.from(userInfo.getUser(), userProfileImage))")
	@Mapping(target = "name", source = "userInfo.user.name")
	@Mapping(target = "admissionYear", source = "userInfo", qualifiedByName = "mapAdmissionYear")
	@Mapping(target = "academicStatus", source = "userInfo", qualifiedByName = "mapAcademicStatus")
	@Mapping(target = "description", source = "userInfo.description")
	@Mapping(target = "phoneNumber", source = "userInfo", qualifiedByName = "mapPhoneNumber")
	@Mapping(target = "isPhoneNumberVisible", source = "userInfo.phoneNumberVisible")
	@Mapping(target = "email", source = "userInfo.user.email")
	@Mapping(target = "socialLinks", source = "userInfo.socialLinks", qualifiedByName = "sortSocialLinksByDomain")
	@Mapping(target = "userTechStack", source = "userInfo.userTechStack", qualifiedByName = "sortStringsAsc")
	@Mapping(target = "userCareer", source = "userInfo.userCareer", qualifiedByName = "sortUserCareer")
	@Mapping(target = "userProject", source = "userInfo.userProject", qualifiedByName = "sortUserProject")
	@Mapping(target = "userInterestTech", source = "userInfo.userInterestTech", qualifiedByName = "sortStringsAsc")
	@Mapping(target = "userInterestDomain", source = "userInfo.userInterestDomain", qualifiedByName = "sortStringsAsc")
	UserInfoDetailResult toDetailResult(UserInfo userInfo, UserProfileImage userProfileImage);

	// 내 동문 수첩 프로필 상세 조회 (전화번호 직접 노출)
	@Mapping(target = "id", source = "userInfo.id")
	@Mapping(target = "profileImage", expression = "java(net.causw.app.main.shared.dto.ProfileImageDto.from(userInfo.getUser(), userProfileImage))")
	@Mapping(target = "name", source = "userInfo.user.name")
	@Mapping(target = "admissionYear", source = "userInfo", qualifiedByName = "mapAdmissionYear")
	@Mapping(target = "academicStatus", source = "userInfo", qualifiedByName = "mapAcademicStatus")
	@Mapping(target = "description", source = "userInfo.description")
	@Mapping(target = "email", source = "userInfo.user.email")
	@Mapping(target = "phoneNumber", source = "userInfo.user.phoneNumber")
	@Mapping(target = "isPhoneNumberVisible", source = "userInfo.phoneNumberVisible")
	@Mapping(target = "socialLinks", source = "userInfo.socialLinks", qualifiedByName = "sortSocialLinksByDomain")
	@Mapping(target = "userTechStack", source = "userInfo.userTechStack", qualifiedByName = "sortStringsAsc")
	@Mapping(target = "userCareer", source = "userInfo.userCareer", qualifiedByName = "sortUserCareer")
	@Mapping(target = "userProject", source = "userInfo.userProject", qualifiedByName = "sortUserProject")
	@Mapping(target = "userInterestTech", source = "userInfo.userInterestTech", qualifiedByName = "sortStringsAsc")
	@Mapping(target = "userInterestDomain", source = "userInfo.userInterestDomain", qualifiedByName = "sortStringsAsc")
	UserInfoDetailResult toMyDetailResult(UserInfo userInfo, UserProfileImage userProfileImage);

	// 동문 수첩 프로필 리스트 조회
	@Mapping(target = "id", source = "userInfo.id")
	@Mapping(target = "profileImage", expression = "java(net.causw.app.main.shared.dto.ProfileImageDto.from(userInfo.getUser(), userProfileImage))")
	@Mapping(target = "name", source = "userInfo.user.name")
	@Mapping(target = "admissionYear", source = "userInfo", qualifiedByName = "mapAdmissionYear")
	@Mapping(target = "academicStatus", source = "userInfo", qualifiedByName = "mapAcademicStatus")
	@Mapping(target = "description", source = "userInfo.description")
	UserInfoSummaryResult toSummaryResult(UserInfo userInfo, UserProfileImage userProfileImage);

	@Named("mapAdmissionYear")
	static String mapAdmissionYear(UserInfo userInfo) {
		return String.format("%s학번",
			userInfo.getUser().getAdmissionYear().toString().substring(2, 4));
	}

	@Named("mapAcademicStatus")
	static String mapAcademicStatus(UserInfo userInfo) {
		return switch (userInfo.getUser().getAcademicStatus()) {
			case ENROLLED -> "재학생";
			case GRADUATED -> "졸업생";
			default -> "기타"; // 미정
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

	@Named("sortSocialLinksByDomain")
	static List<String> sortSocialLinksByDomain(List<String> socialLinks) {
		if (socialLinks == null || socialLinks.isEmpty())
			return List.of();

		// 도메인으로 우선 정렬, 같은 도메인이면 전체 URL로 2차 정렬
		Comparator<String> byDomainThenFullUrl = Comparator
			.comparing(UserInfoMapper::extractSortDomainKey, String.CASE_INSENSITIVE_ORDER)
			.thenComparing(String.CASE_INSENSITIVE_ORDER);

		return socialLinks.stream()
			.filter(s -> s != null && !s.isBlank())
			.map(String::trim)
			.sorted(byDomainThenFullUrl)
			.toList();
	}

	@Named("sortUserCareer")
	static List<UserCareerResult> sortUserCareer(List<UserCareer> careers) {
		if (careers == null || careers.isEmpty()) {
			return List.of();
		}

		return careers.stream()
			.sorted(Comparator
				.comparing(UserInfoMapper::isCurrentCareer, Comparator.reverseOrder())
				.thenComparing(UserCareer::getEndYear, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(UserCareer::getEndMonth, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(UserCareer::getStartYear, Comparator.reverseOrder())
				.thenComparing(UserCareer::getStartMonth, Comparator.reverseOrder()))
			.map(career -> new UserCareerResult(
				career.getId(),
				career.getStartYear(),
				career.getStartMonth(),
				career.getEndYear(),
				career.getEndMonth(),
				career.getDescription()))
			.toList();
	}

	@Named("sortUserProject")
	static List<UserProjectResult> sortUserProject(List<UserProject> projects) {
		if (projects == null || projects.isEmpty()) {
			return List.of();
		}

		return projects.stream()
			.sorted(Comparator
				.comparing(UserInfoMapper::isCurrentProject, Comparator.reverseOrder())
				.thenComparing(UserProject::getEndYear, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(UserProject::getEndMonth, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(UserProject::getStartYear, Comparator.reverseOrder())
				.thenComparing(UserProject::getStartMonth, Comparator.reverseOrder()))
			.map(project -> new UserProjectResult(
				project.getId(),
				project.getStartYear(),
				project.getStartMonth(),
				project.getEndYear(),
				project.getEndMonth(),
				project.getDescription()))
			.toList();
	}

	private static boolean isCurrentCareer(UserCareer career) {
		return career.getEndYear() == null && career.getEndMonth() == null;
	}

	private static boolean isCurrentProject(UserProject project) {
		return project.getEndYear() == null && project.getEndMonth() == null;
	}

	private static String extractSortDomainKey(String url) {
		try {
			String urlToParse = (url.contains("://") || url.startsWith("//")) ? url : "https://" + url;
			String host = new URI(urlToParse).getHost();
			if (host == null || host.isBlank()) {
				return url;
			}
			return host.startsWith("www.") ? host.substring(4) : host;
		} catch (URISyntaxException e) {
			return url;
		}
	}
}
