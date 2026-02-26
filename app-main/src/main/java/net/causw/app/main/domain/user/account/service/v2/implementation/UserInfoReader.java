package net.causw.app.main.domain.user.account.service.v2.implementation;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.api.v2.dto.request.UserInfoListCondition;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.repository.userInfo.UserInfoQueryRepository;
import net.causw.app.main.domain.user.account.repository.userInfo.UserInfoRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserInfoReader {

	private final UserInfoRepository userInfoRepository;
	private final UserInfoQueryRepository userInfoqueryRepository;

	public Page<UserInfo> findAllWithFilter(UserInfoListCondition condition, Pageable pageable) {
		return userInfoqueryRepository.findAllWithFilter(condition, pageable);
	}

	public Optional<UserInfo> findById(String userInfoId) {
		return userInfoRepository.findById(userInfoId);
	}

	public Optional<UserInfo> findByUserId(String userId) {
		return userInfoRepository.findByUserId(userId);
	}
}
