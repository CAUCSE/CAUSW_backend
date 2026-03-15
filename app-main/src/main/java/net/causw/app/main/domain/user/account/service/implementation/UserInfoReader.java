package net.causw.app.main.domain.user.account.service.implementation;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.userInfo.UserCareer;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.entity.userInfo.UserProject;
import net.causw.app.main.domain.user.account.repository.userInfo.UserCareerRepository;
import net.causw.app.main.domain.user.account.repository.userInfo.UserInfoQueryRepository;
import net.causw.app.main.domain.user.account.repository.userInfo.UserInfoRepository;
import net.causw.app.main.domain.user.account.repository.userInfo.UserProjectRepository;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoListCondition;
import net.causw.app.main.shared.entity.BaseEntity;
import net.causw.app.main.shared.exception.errorcode.UserInfoErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 동문 수첩(UserInfo) 및 관련 엔티티(경력, 프로젝트)의 조회 전담.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserInfoReader {

	private final UserInfoRepository userInfoRepository;
	private final UserInfoQueryRepository userInfoQueryRepository;
	private final UserCareerRepository userCareerRepository;
	private final UserProjectRepository userProjectRepository;

	/**
	 * 조건 및 페이징으로 동문 수첩 리스트 조회
	 * @param condition 검색 조건
	 * @param pageable 페이징 정보
	 * @return 동문 수첩 프로필 페이지
	 */
	public Page<UserInfo> findUserInfoWithFilter(UserInfoListCondition condition, Pageable pageable) {
		return userInfoQueryRepository.findAllWithFilter(condition, pageable);
	}

	/**
	 * ID로 동문 수첩 프로필 조회
	 * @param userInfoId 동문 수첩 프로필 ID
	 * @return 동문 수첩 프로필 (없으면 empty)
	 */
	public Optional<UserInfo> findById(String userInfoId) {
		return userInfoRepository.findById(userInfoId);
	}

	/**
	 * 사용자 ID로 동문 수첩 프로필 조회
	 * @param userId 사용자 ID
	 * @return 동문 수첩 프로필 (없으면 empty)
	 */
	public Optional<UserInfo> findByUserId(String userId) {
		return userInfoRepository.findByUserId(userId);
	}

	/**
	 * ID로 경력 조회
	 * @param careerId 경력 ID
	 * @return 경력 엔티티
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception 경력이 존재하지 않을 경우
	 */
	public UserCareer getCareer(String careerId) {
		return userCareerRepository.findById(careerId)
			.orElseThrow(UserInfoErrorCode.USER_CAREER_NOT_FOUND::toBaseException);
	}

	/**
	 * ID로 프로젝트 조회
	 * @param projectId 프로젝트 ID
	 * @return 프로젝트 엔티티
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception 프로젝트가 존재하지 않을 경우
	 */
	public UserProject getProject(String projectId) {
		return userProjectRepository.findById(projectId)
			.orElseThrow(UserInfoErrorCode.USER_PROJECT_NOT_FOUND::toBaseException);
	}

	/**
	 * 동문 수첩 ID로 소속 경력 ID 목록 조회 (시작일 기준 내림차순)
	 * @param userInfoId 동문 수첩 프로필 ID
	 * @return 경력 ID 목록
	 */
	public List<String> findCareerIdsByUserInfoId(String userInfoId) {
		return userCareerRepository
			.findAllCareerByUserInfoIdOrderByStartYearDescStartMonthDesc(userInfoId).stream()
			.map(BaseEntity::getId)
			.toList();
	}

	/**
	 * 동문 수첩 ID로 소속 프로젝트 ID 목록 조회 (시작일 기준 내림차순)
	 * @param userInfoId 동문 수첩 프로필 ID
	 * @return 프로젝트 ID 목록
	 */
	public List<String> findProjectIdsByUserInfoId(String userInfoId) {
		return userProjectRepository
			.findAllProjectByUserInfoIdOrderByStartYearDescStartMonthDesc(userInfoId).stream()
			.map(BaseEntity::getId)
			.toList();
	}
}
