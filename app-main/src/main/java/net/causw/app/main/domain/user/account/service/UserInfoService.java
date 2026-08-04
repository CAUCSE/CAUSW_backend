package net.causw.app.main.domain.user.account.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.asset.file.service.implementation.UserProfileImageReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.enums.userinfo.SortType;
import net.causw.app.main.domain.user.account.enums.userinfo.UserInfoSectionType;
import net.causw.app.main.domain.user.account.service.dto.UserInfoCursor;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoListCondition;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoUpdateCommand;
import net.causw.app.main.domain.user.account.service.dto.result.UserInfoDetailResult;
import net.causw.app.main.domain.user.account.service.dto.result.UserInfoDirectoryResult;
import net.causw.app.main.domain.user.account.service.dto.result.UserInfoSectionResult;
import net.causw.app.main.domain.user.account.service.dto.result.UserInfoSummaryResult;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoCreator;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoReader;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoWriter;
import net.causw.app.main.domain.user.account.service.mapper.UserInfoMapper;
import net.causw.app.main.domain.user.account.service.util.UserInfoCursorManager;
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
	private final UserProfileImageReader userProfileImageReader;
	private final UserInfoCursorManager userInfoCursorManager;

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
		userInfo.update(request.description(), request.isPhoneNumberVisible());
		userInfo.updateSocialLinks(request.socialLinks());
		userInfo.updateTechStack(request.userTechStack());
		userInfo.updateInterestTech(request.userInterestTech());
		userInfo.updateInterestDomain(request.userInterestDomain());

		// 경력/프로젝트 엔티티 동기화
		userInfoWriter.syncCareers(request.userCareer(), userInfo);
		userInfoWriter.syncProjects(request.userProject(), userInfo);

		UserInfo updated = userInfoWriter.save(userInfo);
		UserProfileImage profileImage = userProfileImageReader.findByUserIdOrNull(user.getId());
		return userInfoMapper.toDetailResult(updated, profileImage, user.getDepartment());
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
		User user = userInfo.getUser();

		UserProfileImage profileImage = userProfileImageReader.findByUserIdOrNull(
			userInfo.getUser().getId());
		return userInfoMapper.toDetailResult(userInfo, profileImage, user.getDepartment());
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

		UserProfileImage profileImage = userProfileImageReader.findByUserIdOrNull(user.getId());
		return userInfoMapper.toMyDetailResult(userInfo, profileImage, user.getDepartment());
	}

	/**
	 * 동문 수첩 프로필 리스트 조회 및 검색
	 * @param condition 필터 (검색어 포함)
	 * @param pageNum 페이징
	 * @return 동문 수첩 프로필 리스트
	 */
	@Transactional(readOnly = true)
	public Page<UserInfoSummaryResult> getUserInfoPage(
		UserInfoListCondition condition,
		Integer pageNum,
		String excludeUserId) {

		Pageable pageable = pageableFactory.create(pageNum, StaticValue.USER_LIST_PAGE_SIZE);
		Page<UserInfo> userInfos = userInfoReader.findUserInfoWithFilter(condition, pageable, excludeUserId);

		List<String> userIds = userInfos.getContent().stream()
			.map(ui -> ui.getUser().getId())
			.collect(Collectors.toList());
		Map<String, UserProfileImage> profileImageMap = userProfileImageReader.findMapByUserIds(userIds);

		return userInfos.map(ui -> userInfoMapper.toSummaryResult(ui,
			profileImageMap.get(ui.getUser().getId())));
	}

	/**
	 * 동문 수첩 프로필 리스트 조회 및 검색
	 * @param condition 조회 조건
	 * @param userId 조회 user id
	 * @param cursor 커서
	 * @return 동문수첩 리스트
	 */
	@Transactional(readOnly = true)
	public UserInfoDirectoryResult getUserInfoByCursor(
		UserInfoListCondition condition,
		String userId,
		String cursor) {

		boolean isInitialCursor = cursor == null || cursor.isBlank();
		UserInfoSummaryResult myProfile = isInitialCursor ? getUserInfoSummaryResultByUserId(userId) : null;

		int pageSize = StaticValue.USER_LIST_PAGE_SIZE;
		SortType sortType = resolveSortType(condition);
		String filterHash = userInfoCursorManager.createFilterHash(condition, sortType, userId);

		UserInfoCursor nextCursor = isInitialCursor
			? UserInfoCursor.sectionStartCursor(
				UserInfoSectionType.COFFEE_CHAT_AVAILABLE,
				sortType,
				filterHash)
			: userInfoCursorManager.decode(cursor);
		userInfoCursorManager.validateFilterHash(nextCursor, condition, sortType, userId);

		// 조회를 통해 얻은 section의 개수가 목표개수보다 작다면, 다음 커서를 불러와서 조회 (반복)
		Map<UserInfoSectionType, UserInfoSectionResult> sections = new EnumMap<>(UserInfoSectionType.class);
		for (UserInfoSectionType sectionType : UserInfoSectionType.values()) {
			sections.put(sectionType, new UserInfoSectionResult(sectionType, List.of(), false));
		}
		UserInfoSectionType currentSectionType = nextCursor.section();
		int remainingSize = pageSize;

		while (remainingSize > 0 && currentSectionType != null) {
			Slice<UserInfo> currentSlice = userInfoReader.readCursor(
				condition,
				nextCursor,
				userId,
				remainingSize);
			List<UserInfoSummaryResult> summaryResults = toSummaryResults(currentSlice.getContent());
			sections.put(
				currentSectionType,
				new UserInfoSectionResult(currentSectionType, summaryResults, currentSlice.hasNext()));
			remainingSize -= currentSlice.getContent().size();

			// 해당 slice next가 있다면 조회 완료된것이므로 pass
			if (currentSlice.hasNext()) {
				nextCursor = userInfoCursorManager.nextCursor(
					currentSlice,
					currentSectionType,
					sortType,
					filterHash);
				break;
			}

			// 현재 sectionType 조회완료했는데도 목표 개수 못채웠다면 nextCursor
			currentSectionType = currentSectionType.next();
			if (currentSectionType != null) {
				nextCursor = UserInfoCursor.sectionStartCursor(currentSectionType, sortType, filterHash);
			} else {
				nextCursor = null;
			}

		}

		List<UserInfoSectionResult> result = List.of(
			sections.get(UserInfoSectionType.COFFEE_CHAT_AVAILABLE),
			sections.get(UserInfoSectionType.ALL_MEMBERS));
		return new UserInfoDirectoryResult(
			myProfile,
			result,
			nextCursor != null ? userInfoCursorManager.encode(nextCursor) : null);
	}

	private UserInfoSummaryResult getUserInfoSummaryResultByUserId(String userId) {
		UserInfo userInfo = userInfoReader.findByUserId(userId)
			.orElseThrow(UserInfoErrorCode.USERINFO_NOT_FOUND::toBaseException);
		UserProfileImage profileImage = userProfileImageReader.findByUserIdOrNull(userId);

		return userInfoMapper.toSummaryResult(userInfo, profileImage);
	}

	private List<UserInfoSummaryResult> toSummaryResults(List<UserInfo> userInfos) {
		List<String> userIds = userInfos.stream()
			.map(userInfo -> userInfo.getUser().getId())
			.toList();
		Map<String, UserProfileImage> profileImageMap = userProfileImageReader.findMapByUserIds(userIds);

		return userInfos.stream()
			.map(userInfo -> userInfoMapper.toSummaryResult(
				userInfo,
				profileImageMap.get(userInfo.getUser().getId())))
			.toList();
	}

	private SortType resolveSortType(UserInfoListCondition condition) {
		if (condition.sortType() == null || condition.sortType().isBlank()) {
			return SortType.UPDATED_AT_DESC;
		}
		return SortType.fromString(condition.sortType());
	}
}
