package net.causw.app.main.domain.user.account.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.enums.user.SocialType;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.repository.user.SocialAccountRepository;
import net.causw.app.main.domain.user.account.repository.userInfo.UserInfoRepository;
import net.causw.app.main.domain.user.account.service.dto.request.UserPasswordUpdateCommand;
import net.causw.app.main.domain.user.account.service.dto.result.SocialAccountsResult;
import net.causw.app.main.domain.user.account.service.dto.result.UserMeResult;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserValidator;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.domain.user.auth.service.SocialNativeAuthService;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;
import net.causw.app.main.domain.user.auth.service.dto.AuthTokenPair;
import net.causw.app.main.domain.user.auth.service.dto.OAuthAttributes;
import net.causw.app.main.domain.user.auth.service.implementation.AuthTokenManager;
import net.causw.app.main.domain.user.auth.service.implementation.AuthValidator;
import net.causw.app.main.shared.dto.ProfileImageDto;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAccountService {

	private final UserReader userReader;
	private final UserWriter userWriter;
	private final UserValidator userValidator;
	private final AuthValidator authValidator;
	private final AuthTokenManager authTokenManager;
	private final PasswordEncoder passwordEncoder;
	private final UserInfoRepository userInfoRepository;
	private final SocialAccountRepository socialAccountRepository;
	private final SocialNativeAuthService socialNativeAuthService;

	/**
	 * 소셜 로그인을 통해 생성된 임시 유저(GUEST)의 추가 정보를 등록하고 회원가입 절차를 완료합니다.
	 * <p>
	 * 이 과정에서 닉네임, 전화번호 등의 중복 검사를 수행하며,
	 * 성공 시 유저의 상태를 GUEST에서 AWAIT(승인 대기)으로 변경하고 새로운 인증 토큰을 발급합니다.
	 * </p>
	 *
	 * @param userId       정보를 등록할 유저의 고유 식별자 (PK)
	 * @param nickname     사용할 닉네임 (중복 검사 대상)
	 * @param phoneNumber  사용할 전화번호 (중복 검사 대상)
	 * @param name         사용자의 실명(이름)
	 * @param refreshToken 갱신할 기존 리프레시 토큰
	 * @return {@link AuthResult} 변경된 권한이 반영된 새로운 토큰 세트와 유저 정보
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [INVALID_REGISTRATION_STATUS] 유저 상태가 GUEST가 아닌 경우,
	 * 닉네임 또는 전화번호가 중복된 경우
	 */
	@Transactional
	public AuthResult completeRegistration(String userId, String nickname, String phoneNumber, String name,
		String refreshToken) {
		User guestUser = userReader.findUserById(userId);
		if (guestUser.getState() != UserState.GUEST) {
			throw AuthErrorCode.INVALID_REGISTRATION_STATUS.toBaseException();
		}
		userValidator.checkNicknameDuplication(nickname);
		userValidator.checkPhoneNumDuplication(phoneNumber);
		guestUser.submitRegistration(name, nickname, phoneNumber);
		User updatedUser = userWriter.save(guestUser);
		AuthTokenPair tokens = authTokenManager.issueTokens(updatedUser, refreshToken);
		return AuthResult.of(tokens.accessToken(), updatedUser.getName(), updatedUser.getEmail(),
			ProfileImageDto.from(updatedUser),
			tokens.refreshToken(), updatedUser.isGuest(), updatedUser.isTermsAgreed(),
			updatedUser.isAcademicCertified(), updatedUser.getAcademicStatus());
	}

	/**
	 * 현재 로그인한 사용자의 기본 정보를 조회합니다. 내정보 메인페이지 진입 시 사용합니다.
	 *
	 * @param userId 조회할 사용자의 고유 식별자 (PK)
	 * @return {@link UserMeResult} 내 정보 결과 (이름, 닉네임, 프로필이미지, 입학년도, 직업)
	 */
	@Transactional(readOnly = true)
	public UserMeResult getMyProfile(String userId) {
		User user = userReader.findDetailById(userId);
		UserInfo userInfo = userInfoRepository.findByUserId(userId).orElse(null);
		return UserMeResult.from(user, userInfo);
	}

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
		List<SocialAccount> accounts = socialAccountRepository.findAllByUserId(userId);
		return new SocialAccountsResult(
			accounts.stream().anyMatch(a -> a.getSocialType() == SocialType.GOOGLE),
			accounts.stream().anyMatch(a -> a.getSocialType() == SocialType.KAKAO),
			accounts.stream().anyMatch(a -> a.getSocialType() == SocialType.APPLE));
	}

	/**
	 * 현재 로그인한 사용자에게 소셜 계정을 연동합니다.
	 * <p>
	 * provider 토큰을 검증해 socialId/socialType을 추출합니다.
	 * 해당하는 SocialAccount가 있는지 확인 후, 아래 케이스에 따라 처리합니다.
	 * <ul>
	 *   <li>SocialAccount 없음 + 현재 유저에 동일 provider 없음 → 신규 생성 후 연결</li>
	 *   <li>SocialAccount 있음 + 다른 GUEST 유저에 연결됨 → 현재 유저로 재연결</li>
	 *   <li>SocialAccount 있음 + GUEST가 아닌 다른 유저에 연결됨 → 관리자 문의 에러</li>
	 * </ul>
	 * </p>
	 *
	 * @param userId      연동할 사용자의 고유 식별자 (PK)
	 * @param provider    소셜 provider (kakao, google, apple)
	 * @param accessToken 카카오용 access token (구글/애플은 null)
	 * @param idToken     구글/애플용 id token (카카오는 null)
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [INVALID_REGISTRATION_STATUS] ACTIVE 상태가 아닌 유저가 요청한 경우,
	 * [ALREADY_LINKED_SOCIAL_PROVIDER] 현재 유저에 동일 provider가 이미 연동된 경우,
	 * [SOCIAL_ACCOUNT_LINKED_TO_OTHER_USER] 해당 소셜 계정이 다른 활성 유저에 연결된 경우
	 */
	@Transactional
	public void linkSocialAccount(String userId, String provider, String accessToken, String idToken) {
		User currentUser = userReader.findUserById(userId);

		if (currentUser.getState() != UserState.ACTIVE) {
			throw AuthErrorCode.INVALID_REGISTRATION_STATUS.toBaseException();
		}

		OAuthAttributes attributes = socialNativeAuthService.extractAttributes(provider, accessToken, idToken);

		// 현재 유저에 동일 provider 이미 연동됐는지 먼저 확인 (신규 SocialAccount 존재 여부 무관)
		userValidator.checkAccountExistByUserAndSocialType(currentUser, attributes.socialType());

		Optional<SocialAccount> existingSocialAccount =
			socialAccountRepository.findBySocialIdAndSocialType(attributes.socialId(), attributes.socialType());

		if (existingSocialAccount.isEmpty()) {
			// 1. SocialAccount 없음 → 신규 생성 + 현재 유저에 연결
			userWriter.save(SocialAccount.of(
				attributes.socialType(), attributes.socialId(), attributes.email(), currentUser));
			return;
		}

		// 해당 SocialAccount 있는 경우
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

		SocialAccount socialAccount = socialAccountRepository.findByUserAndSocialType(currentUser, socialType)
			.orElseThrow(AuthErrorCode.SOCIAL_ACCOUNT_NOT_FOUND::toBaseException);

		if (currentUser.isOnlySocialUser()) {
			long linkedCount = socialAccountRepository.findAllByUserId(userId).size();
			if (linkedCount <= 1) {
				throw AuthErrorCode.CANNOT_UNLINK_LAST_LOGIN_METHOD.toBaseException();
			}
		}

		socialAccountRepository.delete(socialAccount);
	}

	/**
	 * 현재 로그인한 사용자의 닉네임을 변경합니다.
	 * <p>
	 * 현재 닉네임과 동일한 값이면 예외를 발생시키고,
	 * 중복 닉네임 검사 후 성공 시 닉네임을 저장합니다.
	 * </p>
	 *
	 * @param userId   닉네임을 변경할 사용자의 고유 식별자 (PK)
	 * @param nickname 변경할 닉네임
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [NICKNAME_SAME_AS_CURRENT] 현재 닉네임과 동일한 경우,
	 * [NICKNAME_ALREADY_EXIST] 이미 사용 중인 닉네임인 경우
	 */
	@Transactional
	public void updateNickname(String userId, String nickname) {
		User user = userReader.findUserById(userId);
		if (nickname.equals(user.getNickname())) {
			throw UserErrorCode.NICKNAME_SAME_AS_CURRENT.toBaseException();
		}
		userValidator.checkNicknameDuplication(nickname);
		user.updateNickname(nickname);
	}

	/**
	 * 사용자가 입력한 닉네임이 DB에 이미 존재하는지 확인합니다.
	 * <p>
	 * 중복된 닉네임이 있을 경우 NICKNAME_DUPLICATED 예외를 발생시킵니다.
	 * </p>
	 *
	 * @param nickname 검사할 닉네임
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception 닉네임이 중복된 경우
	 */
	@Transactional(readOnly = true)
	public void checkNicknameDuplication(String nickname) {
		userValidator.checkNicknameDuplication(nickname);
	}

	/**
	 * 사용자가 입력한 전화번호가 DB에 이미 존재하는지 확인합니다.
	 * <p>
	 * 중복된 전화번호가 있을 경우 PHONE_NUMBER_DUPLICATED 예외를 발생시킵니다.
	 * </p>
	 *
	 * @param phoneNumber 검사할 전화번호
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception 전화번호가 중복된 경우
	 */
	@Transactional(readOnly = true)
	public void checkPhoneNumDuplication(String phoneNumber) {
		userValidator.checkPhoneNumDuplication(phoneNumber);
	}

	/**
	 * 현재 비밀번호를 확인하고 새 비밀번호로 변경합니다.
	 * <p>
	 * 소셜 로그인 전용 계정(비밀번호 없음)은 변경할 수 없으며,
	 * 현재 비밀번호 일치 여부, 새 비밀번호 형식, 새 비밀번호 확인 일치 여부를 검사합니다.
	 * </p>
	 *
	 * @param userId             비밀번호를 변경할 유저의 고유 식별자 (PK)
	 * @param command			비밀번호 재설정 command
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [SOCIAL_USER_CANNOT_CHANGE_PASSWORD] 소셜 로그인 전용 계정인 경우,
	 * [INVALID_CURRENT_PASSWORD] 현재 비밀번호가 일치하지 않는 경우,
	 * [INVALID_PASSWORD_REQUEST] 새 비밀번호 형식이 잘못된 경우,
	 * [PASSWORD_CONFIRM_MISMATCH] 새 비밀번호와 확인 값이 일치하지 않는 경우
	 */
	@Transactional
	public void updatePassword(String userId, UserPasswordUpdateCommand command) {
		User user = userReader.findUserById(userId);

		if (user.isOnlySocialUser()) {
			throw UserErrorCode.SOCIAL_ONLY_USER_CANNOT_CHANGE_PASSWORD.toBaseException();
		}

		if (!passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
			throw UserErrorCode.INVALID_CURRENT_PASSWORD.toBaseException();
		}

		if (!command.newPassword().equals(command.newPasswordConfirm())) {
			throw UserErrorCode.PASSWORD_CONFIRM_MISMATCH.toBaseException();
		}

		authValidator.validatePasswordFormat(command.newPassword());

		user.updatePassword(passwordEncoder.encode(command.newPassword()));
	}
}
