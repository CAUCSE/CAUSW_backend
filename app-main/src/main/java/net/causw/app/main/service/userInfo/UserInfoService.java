package net.causw.app.main.service.userInfo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.userInfo.UserInfo;
import net.causw.app.main.dto.userInfo.UserInfoSearchConditionDto;
import net.causw.app.main.repository.userInfo.UserInfoRepository;
import net.causw.app.main.repository.userInfo.query.UserInfoQueryRepository;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserInfoService {
	private final UserInfoRepository userInfoRepository;
	private final UserInfoQueryRepository userInfoQueryRepository;

	/**
	 * 특정 유저에 대한 동문 수첩 정보가 있는 지 확인
	 * @param user 유저
	 * @return 유저 동문 수첩 정보
	 */
	public UserInfo getUserInfoByUser(User user) {
		return userInfoRepository.findByUserId(user.getId())
			.orElseThrow(() -> new NotFoundException(
					ErrorCode.ROW_DOES_NOT_EXIST,
					MessageUtil.USER_NOT_FOUND
				)
			);
	}

	public Page<UserInfo> searchUserInfo(Pageable pageable, UserInfoSearchConditionDto userInfoSearchCondition) {
		return userInfoQueryRepository.searchUserInfo(userInfoSearchCondition, pageable);
	}

	/**
	 * 유저에 대한 동문수첩이 없을 경우 생성하는 메서드
	 * @param user 유저
	 * @return 유저 동문수첩 정보
	 */
	public UserInfo getOrCreateUserInfoFromUser(User user) {

		return userInfoRepository.findByUserId(user.getId())
			.orElseGet(() -> userInfoRepository.save(UserInfo.of(user)));
	}
}
