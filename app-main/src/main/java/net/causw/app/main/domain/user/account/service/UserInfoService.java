package net.causw.app.main.domain.user.account.service;

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
import net.causw.app.main.domain.user.account.service.util.UserInfoCursorParser;
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
	private final UserInfoCursorParser userInfoCursorParser;

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

	@Transactional(readOnly = true)
	public UserInfoDirectoryResult getUserInfoByCursor(
		UserInfoListCondition listCondition,
		String userId,
		String cursor) {
		boolean isInitialCursor = cursor == null || cursor.isBlank();
		UserInfoSummaryResult myProfile = isInitialCursor ? getMySummaryResult(userId) : null;
		int size = StaticValue.USER_LIST_PAGE_SIZE;
		UserInfoCursor currentCursor = isInitialCursor
			? sectionStartCursor(UserInfoSectionType.COFFEE_CHAT_AVAILABLE, resolveSortType(listCondition))
			: userInfoCursorParser.decode(cursor);

		Slice<UserInfo> currentSlice = userInfoReader.findUserInfoWithFilterByCursor(
			listCondition,
			currentCursor,
			userId,
			size);

		if (currentCursor.section() == UserInfoSectionType.ALL_MEMBERS) {
			List<UserInfoSummaryResult> allMemberItems = toSummaryResults(currentSlice.getContent());
			return new UserInfoDirectoryResult(
				myProfile,
				List.of(sectionResult(UserInfoSectionType.ALL_MEMBERS, allMemberItems, currentSlice.hasNext())),
				nextCursor(currentSlice, UserInfoSectionType.ALL_MEMBERS, currentCursor.sortType()));
		}

		List<UserInfo> coffeeChatUserInfos = currentSlice.getContent();
		int remainingSize = size - coffeeChatUserInfos.size();
		Slice<UserInfo> allMemberSlice = null;

		if (!currentSlice.hasNext() && remainingSize > 0) {
			UserInfoCursor allMemberCursor = sectionStartCursor(
				UserInfoSectionType.ALL_MEMBERS,
				currentCursor.sortType());
			allMemberSlice = userInfoReader.findUserInfoWithFilterByCursor(
				listCondition,
				allMemberCursor,
				userId,
				remainingSize);
		}

		List<UserInfoSectionResult> sections = new java.util.ArrayList<>();
		sections.add(sectionResult(
			UserInfoSectionType.COFFEE_CHAT_AVAILABLE,
			toSummaryResults(coffeeChatUserInfos),
			currentSlice.hasNext()));

		if (allMemberSlice != null) {
			sections.add(sectionResult(
				UserInfoSectionType.ALL_MEMBERS,
				toSummaryResults(allMemberSlice.getContent()),
				allMemberSlice.hasNext()));
		}

		String nextCursor = currentSlice.hasNext()
			? nextCursor(currentSlice, UserInfoSectionType.COFFEE_CHAT_AVAILABLE, currentCursor.sortType())
			: nextAllMemberCursor(allMemberSlice, remainingSize, currentCursor.sortType());

		return new UserInfoDirectoryResult(myProfile, sections, nextCursor);
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

	private UserInfoSectionResult sectionResult(
		UserInfoSectionType type,
		List<UserInfoSummaryResult> items,
		boolean hasNext) {
		return new UserInfoSectionResult(type, items, hasNext);
	}

	private String nextAllMemberCursor(Slice<UserInfo> allMemberSlice, int remainingSize, SortType sortType) {
		if (allMemberSlice != null) {
			return nextCursor(allMemberSlice, UserInfoSectionType.ALL_MEMBERS, sortType);
		}
		if (remainingSize == 0) {
			return userInfoCursorParser.encode(sectionStartCursor(UserInfoSectionType.ALL_MEMBERS, sortType));
		}
		return null;
	}

	private String nextCursor(Slice<UserInfo> slice, UserInfoSectionType section, SortType sortType) {
		if (!slice.hasNext() || slice.getContent().isEmpty()) {
			return null;
		}

		UserInfo lastUserInfo = slice.getContent().getLast();
		User lastUser = lastUserInfo.getUser();
		return userInfoCursorParser.encode(new UserInfoCursor(
			section,
			sortType,
			lastUserInfo.getUpdatedAt(),
			lastUser.getAdmissionYear(),
			lastUser.getName(),
			lastUserInfo.getId()));
	}

	private UserInfoCursor sectionStartCursor(UserInfoSectionType section, SortType sortType) {
		return new UserInfoCursor(section, sortType, null, null, null, null);
	}

	private SortType resolveSortType(UserInfoListCondition condition) {
		if (condition.sortType() == null || condition.sortType().isBlank()) {
			return SortType.UPDATED_AT_DESC;
		}
		return SortType.fromString(condition.sortType());
	}

	private UserInfoSummaryResult getMySummaryResult(String userId) {
		UserInfoSummaryResult myUserInfoSummaryResult;
		UserInfo userInfo = userInfoReader.findByUserId(userId)
			.orElseThrow(UserInfoErrorCode.USERINFO_NOT_FOUND::toBaseException);
		UserProfileImage profileImage = userProfileImageReader.findByUserIdOrNull(userId);
		return userInfoMapper.toSummaryResult(userInfo, profileImage);
	}
}
