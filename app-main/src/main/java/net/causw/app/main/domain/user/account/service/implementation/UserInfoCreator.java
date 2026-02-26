package net.causw.app.main.domain.user.account.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.repository.userInfo.UserInfoRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class UserInfoCreator {

	private final UserInfoRepository userInfoRepository;

	public UserInfo save(UserInfo userInfo) {
		return userInfoRepository.save(userInfo);
	}
}
