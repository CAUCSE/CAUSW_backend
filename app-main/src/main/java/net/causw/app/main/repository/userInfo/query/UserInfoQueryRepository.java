package net.causw.app.main.repository.userInfo.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.causw.app.main.domain.model.entity.userInfo.UserInfo;
import net.causw.app.main.dto.userInfo.UserInfoSearchConditionDto;

public interface UserInfoQueryRepository {

	Page<UserInfo> searchUserInfo(UserInfoSearchConditionDto userInfoSearchCondition, Pageable pageable);
}
