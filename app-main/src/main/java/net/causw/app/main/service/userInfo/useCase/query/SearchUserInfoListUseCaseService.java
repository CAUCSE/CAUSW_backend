package net.causw.app.main.service.userInfo.useCase.query;

import static net.causw.global.constant.StaticValue.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.dto.userInfo.UserInfoSearchCondition;
import net.causw.app.main.dto.userInfo.UserInfoSummaryResponseDto;
import net.causw.app.main.dto.util.dtoMapper.UserDtoMapper;
import net.causw.app.main.service.pageable.PageableFactory;
import net.causw.app.main.service.userInfo.UserInfoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchUserInfoListUseCaseService {

	private final UserInfoService userInfoService;
	private final PageableFactory pageableFactory;
	private final UserDtoMapper userDtoMapper;

	public Page<UserInfoSummaryResponseDto> execute(UserInfoSearchCondition userInfoSearchCondition, Integer pageNum) {
		Pageable pageable = pageableFactory.create(pageNum, DEFAULT_PAGE_SIZE);

		return userInfoService.searchUserInfo(pageable, userInfoSearchCondition)
			.map(userDtoMapper::toUserInfoSummaryResponseDto);
	}
}
