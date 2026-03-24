package net.causw.app.main.domain.user.account.repository.userInfo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.causw.app.main.domain.user.account.api.v1.dto.UserInfoSearchConditionDto;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;

public interface UserInfoQueryV1Repository {

	Page<UserInfo> searchUserInfo(UserInfoSearchConditionDto userInfoSearchCondition, Pageable pageable);
}
