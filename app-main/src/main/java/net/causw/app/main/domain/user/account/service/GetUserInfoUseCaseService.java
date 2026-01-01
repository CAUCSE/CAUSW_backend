package net.causw.app.main.domain.user.account.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.api.dto.userInfo.UserInfoResponseDto;
import net.causw.app.main.api.dto.util.dtoMapper.UserDtoMapper;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserInfoUseCaseService {

	private final UserInfoService userInfoService;
	private final UserEntityService userEntityService;

	public UserInfoResponseDto execute(String userId) {
		User user = userEntityService.findUserByUserId(userId);

		if (user.getState() != UserState.ACTIVE) {
			throw new BadRequestException(
				ErrorCode.INVALID_REQUEST_USER_STATE,
				MessageUtil.USER_INFO_NOT_ACCESSIBLE);
		}

		UserInfo userInfo = userInfoService.getUserInfoByUser(user);

		return UserDtoMapper.INSTANCE.toUserInfoResponseDto(userInfo);
	}
}
