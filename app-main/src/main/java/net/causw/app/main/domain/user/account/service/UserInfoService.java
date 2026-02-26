package net.causw.app.main.domain.user.account.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.service.v2.dto.UserInfoListCondition;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoDetailResponseDto;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoSummaryResponseDto;
import net.causw.app.main.domain.user.account.api.v2.mapper.UserInfoDtoMapper;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserInfoCreator;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserInfoReader;
import net.causw.app.main.shared.exception.errorcode.UserInfoErrorCode;
import net.causw.app.main.shared.pageable.PageableFactory;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserInfoService {

	private final UserInfoReader userInfoReader;
	private final UserInfoDtoMapper userInfoDtoMapper;
	private final UserInfoCreator userInfoCreator;
	private final PageableFactory pageableFactory;

	/**
	 * 사용자에 대한 동문 수첩이 없을 경우 동문 수첩 프로필 생성
	 * @param user 사용자
	 * @return 사용자 동문 수첩 프로필
	 */
	public UserInfo getOrCreateUserInfoFromUser(User user) {
		return userInfoReader.findByUserId(user.getId())
			.orElseGet(() -> userInfoCreator.save(UserInfo.of(user)));
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
	 * @param userId 사용자 ID
	 * @return 내 동문 수첩 프로필 상세
	 */
	@Transactional(readOnly = true)
	public UserInfoDetailResponseDto getMyDetailUserInfo(String userId) {
		UserInfo userInfo = userInfoReader.findByUserId(userId)
			.orElseThrow(UserInfoErrorCode.USERINFO_NOT_FOUND::toBaseException);

		return userInfoDtoMapper.toMyUserInfoDetailResponseDto(userInfo);
	}

	/**
	 * 동문 수첩 프로필 리스트 조회 및 검색
	 * @param condition 필터 (검색어 포함)
	 * @param pageNum 페이징
	 * @return 동문 수첩 프로필 리스트
	 */
	public Page<UserInfoSummaryResponseDto> getUserInfoPage(UserInfoListCondition condition, Integer pageNum) {
		Page<UserInfo> userInfos;
		Pageable pageable = pageableFactory.create(pageNum, StaticValue.USER_LIST_PAGE_SIZE);
		userInfos = userInfoReader.findUserInfoWithFilter(condition, pageable);

		return userInfos.map(userInfoDtoMapper::toUserInfoSummaryResponseDto);
	}
}
