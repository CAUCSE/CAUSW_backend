package net.causw.app.main.domain.user.account.repository.userInfo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoSearchCondition;

public interface UserInfoQueryV1Repository {

	Page<UserInfo> searchUserInfo(UserInfoSearchCondition userInfoSearchCondition, Pageable pageable);
}
