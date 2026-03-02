package net.causw.app.main.domain.user.account.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoDetailResponseDto;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoSummaryResponseDto;
import net.causw.app.main.domain.user.account.api.v2.mapper.UserInfoDtoMapper;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.userInfo.UserCareer;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.entity.userInfo.UserProject;
import net.causw.app.main.domain.user.account.service.dto.request.UserCareerCommand;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoListCondition;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoUpdateDto;
import net.causw.app.main.domain.user.account.service.dto.request.UserProjectCommand;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoCreator;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoReader;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoWriter;
import net.causw.app.main.shared.exception.errorcode.UserInfoErrorCode;
import net.causw.app.main.shared.pageable.PageableFactory;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserInfoService {

	private final UserInfoCreator userInfoCreator;
	private final UserInfoReader userInfoReader;
	private final UserInfoDtoMapper userInfoDtoMapper;
	private final PageableFactory pageableFactory;
	private final UserInfoWriter userInfoWriter;

	/**
	 * 내 동문 수첩 프로필 수정
	 * @param request 수정할 내용
	 * @param user 사용자
	 * @return 사용자 동문 수첩 프로필
	 */
	@Transactional
	public UserInfoDetailResponseDto updateUserInfo(UserInfoUpdateDto request, User user) {
		// 아직 동문 수첩 프로필 생성되지 않았으면 새로 생성
		UserInfo userInfo = userInfoReader.findByUserId(user.getId())
			.orElseGet(() -> userInfoCreator.createAndSave(user));

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
		replaceStrings(userInfo.getUserTechStack(), request.userTechStack());
		replaceStrings(userInfo.getUserInterestTech(), request.userInterestTech());
		replaceStrings(userInfo.getUserInterestDomain(), request.userInterestDomain());

		UserInfo updated = userInfoWriter.save(userInfo);
		return userInfoDtoMapper.toUserInfoDetailResponseDto(updated);
	}

	/**
	 * 동문 수첩 프로필 상세 조회
	 * @param userInfoId 동문 수첩 프로필 ID
	 * @return 동문 수첩 프로필 상세
	 */
	@Transactional(readOnly = true)
	public UserInfoDetailResponseDto getDetailUserInfo(String userInfoId) {
		UserInfo userInfo = userInfoReader.findById(userInfoId)
			.orElseThrow(UserInfoErrorCode.USERINFO_NOT_FOUND::toBaseException);

		return userInfoDtoMapper.toUserInfoDetailResponseDto(userInfo);
	}

	/**
	 * 내 동문 수첩 프로필 상세 조회
	 * @param user 사용자
	 * @return 내 동문 수첩 프로필 상세
	 */
	@Transactional
	public UserInfoDetailResponseDto getMyDetailUserInfo(User user) {
		// 아직 동문 수첩 프로필 생성되지 않았으면 새로 생성
		UserInfo userInfo = userInfoReader.findByUserId(user.getId())
			.orElseGet(() -> userInfoCreator.createAndSave(user));

		return userInfoDtoMapper.toMyUserInfoDetailResponseDto(userInfo);
	}

	/**
	 * 동문 수첩 프로필 리스트 조회 및 검색
	 * @param condition 필터 (검색어 포함)
	 * @param pageNum 페이징
	 * @return 동문 수첩 프로필 리스트
	 */
	@Transactional(readOnly = true)
	public Page<UserInfoSummaryResponseDto> getUserInfoPage(UserInfoListCondition condition, Integer pageNum) {
		Page<UserInfo> userInfos;
		Pageable pageable = pageableFactory.create(pageNum, StaticValue.USER_LIST_PAGE_SIZE);
		userInfos = userInfoReader.findUserInfoWithFilter(condition, pageable);

		return userInfos.map(userInfoDtoMapper::toUserInfoSummaryResponseDto);
	}

	// 이력 사항 업데이트
	private void updateUserCareer(List<UserCareerCommand> dtoList, UserInfo userInfo) {
		if (dtoList == null)
			return;
		Set<String> requests = new HashSet<>();

		for (UserCareerCommand dto : dtoList) {
			dto.validateDate();
			if (dto.id() == null) { // 이력 사항 추가
				UserCareer created = userInfoWriter.saveCareer(
					UserCareer.of(userInfo,
						dto.startYear(), dto.startMonth(),
						dto.endYear(), dto.endMonth(),
						dto.description()));
				requests.add(created.getId());
			} else { // 이력 사항 수정
				UserCareer exists = userInfoWriter.getCareer(dto.id());
				exists.update(
					dto.startYear(), dto.startMonth(),
					dto.endYear(), dto.endMonth(),
					dto.description());
				requests.add(dto.id());
			}
		}
		// 이력 사항 삭제
		List<String> currentCareerList = userInfoWriter.findCareerByUserInfoId(userInfo.getId());
		List<String> toDelete = currentCareerList.stream().filter(id -> !requests.contains(id)).toList();
		userInfoWriter.deleteCareerByIds(toDelete);
	}

	// 대표 프로젝트 업데이트
	private void updateUserProject(List<UserProjectCommand> dtoList, UserInfo userInfo) {
		if (dtoList == null)
			return;
		Set<String> requests = new HashSet<>();

		for (UserProjectCommand dto : dtoList) {
			dto.validateDate();
			if (dto.id() == null) { // 데표 프로젝트 추가
				UserProject created = userInfoWriter.saveProject(
					UserProject.of(userInfo,
						dto.startYear(), dto.startMonth(),
						dto.endYear(), dto.endMonth(),
						dto.description()));
				requests.add(created.getId());
			} else { // 대표 프로젝트 수정
				UserProject exists = userInfoWriter.getProject(dto.id());
				exists.update(
					dto.startYear(), dto.startMonth(),
					dto.endYear(), dto.endMonth(),
					dto.description());
				requests.add(dto.id());
			}
		}
		// 대표 프로젝트 삭제
		List<String> currentProjectList = userInfoWriter.findProjectByUserInfoId(userInfo.getId());
		List<String> toDelete = currentProjectList.stream().filter(id -> !requests.contains(id)).toList();
		userInfoWriter.deleteProjectByIds(toDelete);
	}

	private void replaceStrings(Set<String> target, List<String> incoming) {
		target.clear();
		if (incoming == null)
			return;

		incoming.stream()
			.filter(s -> s != null && !s.isBlank())
			.forEach(target::add);
	}
}
