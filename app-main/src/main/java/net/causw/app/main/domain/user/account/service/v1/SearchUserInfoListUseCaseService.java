package net.causw.app.main.domain.user.account.service.v1;

import static net.causw.global.constant.StaticValue.DEFAULT_PAGE_SIZE;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.shared.entity.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.service.v2.implementation.UserProfileImageReader;
import net.causw.app.main.domain.user.account.api.v1.dto.UserInfoSearchConditionDto;
import net.causw.app.main.domain.user.account.api.v1.dto.UserInfoSummaryResponseDto;
import net.causw.app.main.domain.user.account.api.v1.mapper.UserDtoMapper;
import net.causw.app.main.shared.pageable.PageableFactory;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchUserInfoListUseCaseService {

	private final UserInfoV1Service userInfoV1Service;
	private final PageableFactory pageableFactory;
	private final UserDtoMapper userDtoMapper;
	private final UserProfileImageReader userProfileImageReader;

	public Page<UserInfoSummaryResponseDto> execute(UserInfoSearchConditionDto userInfoSearchCondition,
		Integer pageNum) {
		Pageable pageable = pageableFactory.create(pageNum, DEFAULT_PAGE_SIZE);
		Page<UserInfo> userInfos = userInfoV1Service.searchUserInfo(pageable, userInfoSearchCondition);
		var userIds = userInfos.map(UserInfo::getUser).map(BaseEntity::getId).stream().toList();
		Map<String, UserProfileImage> mapByUserIds = userProfileImageReader.findMapByUserIds(userIds);
		return userInfos
			.map(userInfo -> userDtoMapper.toUserInfoSummaryResponseDto(
				userInfo, mapByUserIds.get(userInfo.getUser().getId()))
			);
	}
}
