package net.causw.app.main.domain.user.account.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.userInfo.UserCareer;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.entity.userInfo.UserProject;
import net.causw.app.main.domain.user.account.service.dto.request.UserCareerCommand;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoListCondition;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoUpdateCommand;
import net.causw.app.main.domain.user.account.service.dto.request.UserProjectCommand;
import net.causw.app.main.domain.user.account.service.dto.result.UserInfoDetailResult;
import net.causw.app.main.domain.user.account.service.dto.result.UserInfoSummaryResult;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoCreator;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoReader;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoWriter;
import net.causw.app.main.domain.user.account.service.mapper.UserInfoMapper;
import net.causw.app.main.shared.exception.errorcode.UserInfoErrorCode;
import net.causw.app.main.shared.pageable.PageableFactory;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserInfoService {

	private final UserInfoCreator userInfoCreator;
	private final UserInfoReader userInfoReader;
	private final UserInfoMapper userInfoMapper;
	private final PageableFactory pageableFactory;
	private final UserInfoWriter userInfoWriter;

	/**
	 * 내 동문 수첩 프로필 수정
	 * @param request 수정할 내용
	 * @param user 사용자
	 * @return 사용자 동문 수첩 프로필
	 */
	@Transactional
	public UserInfoDetailResult updateUserInfo(UserInfoUpdateCommand request, User user) {
		// 아직 동문 수첩 프로필 생성되지 않았으면 새로 생성
		UserInfo userInfo = userInfoReader.findByUserId(user.getId())
			.orElseGet(() -> userInfoCreator.createAndSave(user));

		// 소개글, 직업, SNS, 연락처 공개 여부 업데이트
		List<String> socialLinks = request.socialLinks() == null ? List.of() : request.socialLinks();
		userInfo.update(
			request.description(),
			request.job(),
			socialLinks,
			request.isPhoneNumberVisible());

		// 사용자 이력, 대표 프로젝트 업데이트
		updateUserCareer(request.userCareer(), userInfo);
		updateUserProject(request.userProject(), userInfo);

		// 기술 스택, 관심 기술, 관심 도메인 업데이트
		replaceStrings(userInfo.getUserTechStack(), request.userTechStack());
		replaceStrings(userInfo.getUserInterestTech(), request.userInterestTech());
		replaceStrings(userInfo.getUserInterestDomain(), request.userInterestDomain());

		UserInfo updated = userInfoWriter.save(userInfo);
		return userInfoMapper.toDetailResult(updated);
	}

	/**
	 * 동문 수첩 프로필 상세 조회
	 * @param userInfoId 동문 수첩 프로필 ID
	 * @return 동문 수첩 프로필 상세
	 */
	@Transactional(readOnly = true)
	public UserInfoDetailResult getDetailUserInfo(String userInfoId) {
		UserInfo userInfo = userInfoReader.findById(userInfoId)
			.orElseThrow(UserInfoErrorCode.USERINFO_NOT_FOUND::toBaseException);

		return userInfoMapper.toDetailResult(userInfo);
	}

	/**
	 * 내 동문 수첩 프로필 상세 조회
	 * @param user 사용자
	 * @return 내 동문 수첩 프로필 상세
	 */
	@Transactional
	public UserInfoDetailResult getMyDetailUserInfo(User user) {
		// 아직 동문 수첩 프로필 생성되지 않았으면 새로 생성
		UserInfo userInfo = userInfoReader.findByUserId(user.getId())
			.orElseGet(() -> userInfoCreator.createAndSave(user));

		return userInfoMapper.toMyDetailResult(userInfo);
	}

	/**
	 * 동문 수첩 프로필 리스트 조회 및 검색
	 * @param condition 필터 (검색어 포함)
	 * @param pageNum 페이징
	 * @return 동문 수첩 프로필 리스트
	 */
	@Transactional(readOnly = true)
	public Page<UserInfoSummaryResult> getUserInfoPage(UserInfoListCondition condition, Integer pageNum) {
		Pageable pageable = pageableFactory.create(pageNum, StaticValue.USER_LIST_PAGE_SIZE);
		Page<UserInfo> userInfos = userInfoReader.findUserInfoWithFilter(condition, pageable);

		return userInfos.map(userInfoMapper::toSummaryResult);
	}

	// 이력 사항 업데이트
	private void updateUserCareer(List<UserCareerCommand> dtoList, UserInfo userInfo) {
		if (dtoList == null)
			return;
		Set<String> requests = new HashSet<>();
		int currentYear = LocalDate.now().getYear();

		for (UserCareerCommand dto : dtoList) {
			dto.validateDate(currentYear);
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
		int currentYear = LocalDate.now().getYear();

		for (UserProjectCommand dto : dtoList) {
			dto.validateDate(currentYear);
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
