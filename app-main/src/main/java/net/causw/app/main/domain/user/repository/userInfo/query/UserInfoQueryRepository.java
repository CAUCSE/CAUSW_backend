package net.causw.app.main.domain.user.repository.userInfo.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.causw.app.main.domain.moving.dto.userInfo.UserInfoSearchConditionDto;
import net.causw.app.main.domain.user.entity.userInfo.UserInfo;

public interface UserInfoQueryRepository {

	Page<UserInfo> searchUserInfo(UserInfoSearchConditionDto userInfoSearchCondition, Pageable pageable);
}
