package net.causw.app.main.domain.user.account.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.SocialType;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.dto.result.SocialAccountsResult;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountLinker;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountReader;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserValidator;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.domain.user.auth.service.dto.OAuthAttributes;
import net.causw.app.main.domain.user.auth.service.implementation.OAuthAttributesResolver;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SocialLinkService {

	private final UserReader userReader;
	private final UserWriter userWriter;
	private final UserValidator userValidator;
	private final SocialAccountReader socialAccountReader;
	private final OAuthAttributesResolver oAuthAttributesResolver;
	private final SocialAccountLinker socialAccountLinker;

	/**
	 * 현재 로그인한 사용자의 소셜 계정 연동 현황을 조회합니다.
	 * <p>
	 * provider(GOOGLE, KAKAO, APPLE)별 연동 여부를 반환합니다.
	 * JWT 필터에서 유저 존재가 이미 보장되므로 별도의 유저 조회 없이 소셜 계정만 조회합니다.
	 * </p>
	 *
	 * @param userId 조회할 사용자의 고유 식별자 (PK)
	 * @return {@link SocialAccountsResult} provider별 연동 여부
	 */
	@Transactional(readOnly = true)
	public SocialAccountsResult getSocialAccounts(String userId) {
		List<SocialAccount> accounts = socialAccountReader.findAllByUserId(userId);
		return new SocialAccountsResult(
			accounts.stream().anyMatch(a -> a.getSocialType() == SocialType.GOOGLE),
			accounts.stream().anyMatch(a -> a.getSocialType() == SocialType.KAKAO),
			accounts.stream().anyMatch(a -> a.getSocialType() == SocialType.APPLE));
	}

	/**
	 * Native 방식으로 소셜 계정을 연동합니다. (앱 클라이언트용)
	 * <p>
	 * provider 토큰을 검증해 socialId/socialType을 추출한 뒤 연동 정책을 적용합니다.
	 * </p>
	 *
	 * @param userId      연동할 사용자의 고유 식별자 (PK)
	 * @param provider    소셜 provider (kakao, google, apple)
	 * @param accessToken 카카오용 access token (구글/애플은 null)
	 * @param idToken     구글/애플용 id token (카카오는 null)
	 */
	@Transactional
	public void linkSocialAccount(String userId, String provider, String accessToken, String idToken) {
		OAuthAttributes attributes = oAuthAttributesResolver.resolveAttributes(provider, accessToken, idToken);
		socialAccountLinker.applyLinkingPolicy(userId, attributes.socialType(), attributes.socialId(),
			attributes.email());
	}

	/**
	 * 소셜 계정 연동 가능 여부를 사전 검증합니다. (OAuth 연동 init 엔드포인트용)
	 * <p>
	 * ACTIVE 상태인지, 동일 provider가 이미 연동되어 있지 않은지 확인합니다.
	 * OAuth 플로우 진입 전에 호출하여 연동 불가 상태를 미리 차단합니다.
	 * </p>
	 *
	 * @param userId   검증할 사용자의 고유 식별자 (PK)
	 * @param provider 소셜 provider (kakao, google, apple)
	 */
	@Transactional(readOnly = true)
	public void validateLinkable(String userId, String provider) {
		User user = userReader.findUserById(userId);
		if (user.getState() != UserState.ACTIVE) {
			throw AuthErrorCode.INVALID_REGISTRATION_STATUS.toBaseException();
		}
		SocialType socialType = SocialType.from(provider.toLowerCase(Locale.ROOT));
		userValidator.checkAccountExistByUserAndSocialType(user, socialType);
	}

	/**
	 * 현재 로그인한 사용자의 소셜 계정 연동을 해제합니다.
	 * <p>
	 * 비밀번호가 없는 계정(소셜 전용)에서 마지막 남은 소셜 계정을 해제하려 할 경우 예외를 발생시킵니다.
	 * </p>
	 *
	 * @param userId   연동을 해제할 사용자의 고유 식별자 (PK)
	 * @param provider 해제할 소셜 provider (kakao, google, apple)
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [INVALID_REGISTRATION_STATUS] ACTIVE 상태가 아닌 유저가 요청한 경우,
	 * [SOCIAL_ACCOUNT_NOT_FOUND] 해당 provider가 연동되어 있지 않은 경우,
	 * [CANNOT_UNLINK_LAST_LOGIN_METHOD] 비밀번호 없는 계정의 마지막 소셜 계정을 해제하려는 경우
	 */
	@Transactional
	public void unlinkSocialAccount(String userId, String provider) {
		User currentUser = userReader.findUserById(userId);

		if (currentUser.getState() != UserState.ACTIVE) {
			throw AuthErrorCode.INVALID_REGISTRATION_STATUS.toBaseException();
		}

		SocialType socialType = SocialType.from(provider.toLowerCase(Locale.ROOT));

		SocialAccount socialAccount = socialAccountReader.findByUserIdAndSocialTypeOrElseThrow(userId, socialType);

		if (currentUser.isOnlySocialUser()) {
			long linkedCount = socialAccountReader.countByUserId(userId);
			if (linkedCount <= 1) {
				throw AuthErrorCode.CANNOT_UNLINK_LAST_LOGIN_METHOD.toBaseException();
			}
		}

		userWriter.deleteSocialAccount(socialAccount);
	}
}
