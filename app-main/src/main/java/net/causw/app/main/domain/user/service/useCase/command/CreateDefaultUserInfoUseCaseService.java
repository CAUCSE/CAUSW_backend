package net.causw.app.main.domain.user.service.useCase.command;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.entity.user.User;
import net.causw.app.main.domain.user.event.CertifiedUserCreatedEvent;
import net.causw.app.main.domain.user.service.UserEntityService;
import net.causw.app.main.domain.user.service.UserInfoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateDefaultUserInfoUseCaseService {

	private final UserEntityService userEntityService;
	private final UserInfoService userInfoService;

	@EventListener // 동문수첩 기본 프로필 생성 실패시, 학적 인증과 함께 롤백
	public void createDefaultProfile(CertifiedUserCreatedEvent event) {
		User user = userEntityService.findUserByUserId(event.userId());

		userInfoService.getOrCreateUserInfoFromUser(user);
	}
}
