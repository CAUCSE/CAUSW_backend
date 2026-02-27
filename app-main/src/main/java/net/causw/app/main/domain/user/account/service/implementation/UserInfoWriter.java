package net.causw.app.main.domain.user.account.service.implementation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.api.v2.dto.request.UserInfoUpdateRequestDto;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserCareerDto;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserProjectDto;
import net.causw.app.main.domain.user.account.entity.userInfo.UserCareer;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.entity.userInfo.UserProject;
import net.causw.app.main.domain.user.account.repository.userInfo.UserCareerRepository;
import net.causw.app.main.domain.user.account.repository.userInfo.UserInfoRepository;
import net.causw.app.main.domain.user.account.repository.userInfo.UserProjectRepository;
import net.causw.app.main.domain.user.account.util.UserInfoValidator;
import net.causw.app.main.shared.entity.BaseEntity;
import net.causw.app.main.shared.exception.errorcode.UserInfoErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class UserInfoWriter {

	private final UserInfoRepository userInfoRepository;
	private final UserInfoValidator userInfoValidator;
	private final UserCareerRepository userCareerRepository;
	private final UserProjectRepository userProjectRepository;

	public UserInfo update(UserInfoUpdateRequestDto request, UserInfo userInfo) {
		// 소개글, 직업, SNS, 전화번호 공개 여부, 메세지 공개 여부 업데이트
		List<String> socialLinks = request.socialLinks() == null ? List.of() : request.socialLinks();

		userInfo.update(
			request.description(),
			request.job(),
			socialLinks,
			request.isPhoneNumberVisible(),
			request.isMessageVisible());

		// 사용자 이력, 대표 프로젝트 업데이트
		updateUserCareer(request.userCareer(), userInfo);
		updateUserProject(request.userProject(), userInfo);

		// 기술 스택, 관심 기술, 관심 도메인 업데이트
		updateUserTechStack(request.userTechStack(), userInfo);
		updateUserInterestTech(request.userInterestTech(), userInfo);
		updateUserInterestDomain(request.userInterestDomain(), userInfo);

		return userInfoRepository.save(userInfo);
	}

	private void updateUserCareer(List<UserCareerDto> userCareerDtoList, UserInfo userInfo) {
		Set<String> requestedIdSet = new HashSet<>();

		if (userCareerDtoList == null)
			return;
		for (UserCareerDto userCareerDto : userCareerDtoList) {
			userInfoValidator.validateUserCareerDate(userCareerDto);

			if (userCareerDto.id() == null) { // 경력 사항 추가
				UserCareer userCareer = UserCareer.of(
					userInfo,
					userCareerDto.startYear(), userCareerDto.startMonth(),
					userCareerDto.endYear(), userCareerDto.endMonth(),
					userCareerDto.description());

				UserCareer newCareer = userCareerRepository.save(userCareer);
				requestedIdSet.add(newCareer.getId());
			} else { // 경력 사항 수정
				UserCareer userCareer = userCareerRepository.findById(userCareerDto.id())
					.orElseThrow(UserInfoErrorCode.USER_CAREER_NOT_FOUND::toBaseException);

				userCareer.update(
					userCareerDto.startYear(), userCareerDto.startMonth(),
					userCareerDto.endYear(), userCareerDto.endMonth(),
					userCareerDto.description());

				requestedIdSet.add(userCareerDto.id());
			}
		}
		// 경력 사항 삭제
		List<String> idToDeleteList = userCareerRepository
			.findAllCareerByUserInfoIdOrderByStartYearDescStartMonthDesc(userInfo.getId()).stream()
			.map(BaseEntity::getId)
			.filter(id -> !requestedIdSet.contains(id)).toList();

		if (!idToDeleteList.isEmpty()) {
			userCareerRepository.deleteAllByIdInBatch(idToDeleteList);
		}
	}

	private void updateUserProject(List<UserProjectDto> userProjectDtoList, UserInfo userInfo) {
		Set<String> requestedIdSet = new HashSet<>();

		if (userProjectDtoList == null)
			return;
		for (UserProjectDto userProjectDto : userProjectDtoList) {
			userInfoValidator.validateUserProjectDate(userProjectDto);

			if (userProjectDto.id() == null) { // 대표 프로젝트 추가
				UserProject userProject = UserProject.of(
					userInfo,
					userProjectDto.startYear(), userProjectDto.startMonth(),
					userProjectDto.endYear(), userProjectDto.endMonth(),
					userProjectDto.description());

				UserProject newProject = userProjectRepository.save(userProject);
				requestedIdSet.add(newProject.getId());
			} else { // 대표 프로젝트 수정
				UserProject userProject = userProjectRepository.findById(userProjectDto.id())
					.orElseThrow(UserInfoErrorCode.USER_PROJECT_NOT_FOUND::toBaseException);

				userProject.update(
					userProjectDto.startYear(), userProjectDto.startMonth(),
					userProjectDto.endYear(), userProjectDto.endMonth(),
					userProjectDto.description());

				requestedIdSet.add(userProjectDto.id());
			}
		}
		// 대표 프로젝트 삭제
		List<String> idToDeleteList = userProjectRepository
			.findAllProjectByUserInfoIdOrderByStartYearDescStartMonthDesc(userInfo.getId()).stream()
			.map(BaseEntity::getId)
			.filter(id -> !requestedIdSet.contains(id)).toList();

		if (!idToDeleteList.isEmpty()) {
			userProjectRepository.deleteAllByIdInBatch(idToDeleteList);
		}
	}

	private void updateUserTechStack(List<String> techStacks, UserInfo userInfo) {
		userInfo.getUserTechStack().clear();
		if (techStacks == null)
			return;

		techStacks.stream()
			.filter(s -> s != null && !s.isBlank())
			.forEach(userInfo.getUserTechStack()::add);
	}

	private void updateUserInterestTech(List<String> interestTech, UserInfo userInfo) {
		userInfo.getUserInterestTech().clear();
		if (interestTech == null)
			return;

		interestTech.stream()
			.filter(s -> s != null && !s.isBlank())
			.forEach(userInfo.getUserInterestTech()::add);
	}

	private void updateUserInterestDomain(List<String> interestDomain, UserInfo userInfo) {
		userInfo.getUserInterestDomain().clear();
		if (interestDomain == null)
			return;

		interestDomain.stream()
			.filter(s -> s != null && !s.isBlank())
			.forEach(userInfo.getUserInterestDomain()::add);
	}
}
