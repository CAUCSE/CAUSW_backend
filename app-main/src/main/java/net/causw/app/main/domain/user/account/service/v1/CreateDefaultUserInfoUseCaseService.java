package net.causw.app.main.domain.user.account.service.v1;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.academic.event.CertifiedUserCreatedEvent;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateDefaultUserInfoUseCaseService {

	private final UserEntityService userEntityService;
	private final UserInfoV1Service userInfoV1Service;

	@EventListener // 동문수첩 기본 프로필 생성 실패시, 학적 인증과 함께 롤백
	public void createDefaultProfile(CertifiedUserCreatedEvent event) {
		User user = userEntityService.findUserByUserId(event.userId());

		userInfoV1Service.getOrCreateUserInfoFromUser(user);
	}
}
