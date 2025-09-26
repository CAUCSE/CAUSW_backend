package net.causw.app.main.service.userInfo.useCase.query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.userInfo.UserInfo;
import net.causw.app.main.domain.model.enums.user.UserState;
import net.causw.app.main.dto.userInfo.UserInfoResponseDto;
import net.causw.app.main.dto.util.dtoMapper.UserDtoMapper;
import net.causw.app.main.service.user.UserEntityService;
import net.causw.app.main.service.userInfo.UserInfoService;
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
				MessageUtil.USER_INFO_NOT_ACCESSIBLE
			);
		}

		UserInfo userInfo = userInfoService.getUserInfoByUser(user);

		return UserDtoMapper.INSTANCE.toUserInfoResponseDto(userInfo);
	}
}
