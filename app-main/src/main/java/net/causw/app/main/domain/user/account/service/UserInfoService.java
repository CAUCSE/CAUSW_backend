package net.causw.app.main.domain.user.account.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoListCondition;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoUpdateCommand;
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

		// 동문 수첩 정보 및 컬렉션 필드(소셜링크, 기술스택, 관심기술, 관심 도메인 등) 업데이트
		userInfo.update(request.description(), request.job(), request.isPhoneNumberVisible());
		userInfo.updateSocialLinks(request.socialLinks());
		userInfo.updateTechStack(request.userTechStack());
		userInfo.updateInterestTech(request.userInterestTech());
		userInfo.updateInterestDomain(request.userInterestDomain());

		// 경력/프로젝트 엔티티 동기화
		userInfoWriter.syncCareers(request.userCareer(), userInfo);
		userInfoWriter.syncProjects(request.userProject(), userInfo);

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

}
