package net.causw.app.main.domain.user.account.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.api.v2.dto.request.UserInfoUpdateRequestDto;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoDetailResponseDto;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoSummaryResponseDto;
import net.causw.app.main.domain.user.account.api.v2.mapper.UserInfoDtoMapper;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoListCondition;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoCreator;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoReader;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoWriter;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
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
	private final UserReader userReader;
	private final UserInfoWriter userInfoWriter;

	/**
	 * 내 동문 수첩 프로필 수정
	 * @param request 수정할 내용
	 * @param user 사용자
	 * @return 사용자 동문 수첩 프로필
	 */
	@Transactional
	public UserInfoDetailResponseDto updateUserInfo(UserInfoUpdateRequestDto request, User user) {
		// 아직 동문 수첩 프로필 생성되지 않았으면 새로 생성
		UserInfo userInfo = userInfoReader.findByUserId(user.getId())
			.orElseGet(() -> userInfoCreator.save(UserInfo.of(user)));

		userInfo = userInfoWriter.update(request, userInfo);

		return userInfoDtoMapper.toUserInfoDetailResponseDto(userInfo);
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
	@Transactional
	public UserInfoDetailResponseDto getMyDetailUserInfo(String userId) {
		// 아직 동문 수첩 프로필 생성되지 않았으면 새로 생성
		UserInfo userInfo = userInfoReader.findByUserId(userId)
			.orElseGet(() -> userInfoCreator.save(UserInfo.of(userReader.findUserById(userId))));

		return userInfoDtoMapper.toMyUserInfoDetailResponseDto(userInfo);
	}

	/**
	 * 동문 수첩 프로필 리스트 조회 및 검색
	 * @param condition 필터 (검색어 포함)
	 * @param pageNum 페이징
	 * @return 동문 수첩 프로필 리스트
	 */
	@Transactional
	public Page<UserInfoSummaryResponseDto> getUserInfoPage(UserInfoListCondition condition, Integer pageNum) {
		Page<UserInfo> userInfos;
		Pageable pageable = pageableFactory.create(pageNum, StaticValue.USER_LIST_PAGE_SIZE);
		userInfos = userInfoReader.findUserInfoWithFilter(condition, pageable);

		return userInfos.map(userInfoDtoMapper::toUserInfoSummaryResponseDto);
	}
}
