package net.causw.app.main.domain.user.account.service.implementation;

import java.util.Optional;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.SocialType;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.repository.user.SocialAccountRepository;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 소셜 계정 연동 공통 정책을 적용하는 컴포넌트입니다.
 * <p>
 * OAuth 연동 플로우({@link net.causw.app.main.domain.user.auth.service.CustomOAuth2UserService})와
 * Native 연동 플로우({@link net.causw.app.main.domain.user.account.service.SocialLinkService}) 양쪽에서
 * 공통으로 사용합니다.
 */
@Component
@RequiredArgsConstructor
public class SocialAccountLinker {

	private final UserReader userReader;
	private final UserWriter userWriter;
	private final UserValidator userValidator;
	private final SocialAccountRepository socialAccountRepository;

	/**
	 * 소셜 계정 연동 공통 정책을 적용합니다.
	 * <ul>
	 *   <li>SocialAccount 없음 + 현재 유저에 동일 provider 없음 → 신규 생성 후 연결</li>
	 *   <li>SocialAccount 있음 + 다른 GUEST 유저에 연결됨 → 현재 유저로 재연결</li>
	 *   <li>SocialAccount 있음 + GUEST가 아닌 다른 유저에 연결됨 → 관리자 문의 에러</li>
	 * </ul>
	 *
	 * @param userId     연동할 사용자의 고유 식별자 (PK)
	 * @param socialType 소셜 provider 타입
	 * @param socialId   소셜 provider의 사용자 고유 ID
	 * @param email      소셜 provider에서 제공한 이메일
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [INVALID_REGISTRATION_STATUS] ACTIVE 상태가 아닌 유저가 요청한 경우,
	 * [ALREADY_LINKED_SOCIAL_PROVIDER] 현재 유저에 동일 provider가 이미 연동된 경우,
	 * [SOCIAL_ACCOUNT_LINKED_TO_OTHER_USER] 해당 소셜 계정이 다른 활성 유저에 연결된 경우
	 */
	public void applyLinkingPolicy(String userId, SocialType socialType, String socialId, String email) {
		User currentUser = userReader.findUserById(userId);

		if (currentUser.getState() != UserState.ACTIVE) {
			throw AuthErrorCode.INVALID_REGISTRATION_STATUS.toBaseException();
		}

		// 현재 유저에 동일 provider 이미 연동됐는지 먼저 확인 (신규 SocialAccount 존재 여부 무관)
		userValidator.checkAccountExistByUserAndSocialType(currentUser, socialType);

		Optional<SocialAccount> existingSocialAccount = socialAccountRepository
			.findBySocialIdAndSocialType(socialId, socialType);

		if (existingSocialAccount.isEmpty()) {
			// 1. SocialAccount 없음 → 신규 생성 + 현재 유저에 연결
			userWriter.save(SocialAccount.of(socialType, socialId, email, currentUser));
			return;
		}

		SocialAccount socialAccount = existingSocialAccount.get();
		User linkedUser = socialAccount.getUser();

		if (linkedUser.getState() == UserState.GUEST) {
			// 2. 다른 GUEST 유저에 연결됨 → 현재 유저로 재연결
			socialAccount.relink(currentUser);
			return;
		}

		// 3. GUEST가 아닌 다른 유저에 연결됨 → 관리자 문의
		throw AuthErrorCode.SOCIAL_ACCOUNT_LINKED_TO_OTHER_USER.toBaseException();
	}
}
