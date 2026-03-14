package net.causw.app.main.domain.user.auth.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.dto.request.UserRegisterDto;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountReader;
import net.causw.app.main.domain.user.account.service.implementation.UserPushTokenWriter;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserValidator;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;
import net.causw.app.main.domain.user.auth.service.dto.AuthTokenPair;
import net.causw.app.main.domain.user.auth.service.dto.EmailFindResult;
import net.causw.app.main.domain.user.auth.service.implementation.AuthTokenManager;
import net.causw.app.main.domain.user.auth.service.implementation.AuthValidator;
import net.causw.app.main.domain.user.auth.service.implementation.EmailVerificationValidator;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 인증 및 인가를 담당하는 서비스입니다.
 * <p>
 * 회원가입, 이메일 로그인, 토큰 재발급, 로그아웃 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserReader userReader;
	private final UserWriter userWriter;
	private final PasswordEncoder passwordEncoder;
	private final UserValidator userValidator;
	private final AuthValidator authValidator;
	private final AuthTokenManager authTokenManager;
	private final UserPushTokenWriter userPushTokenWriter;
	private final EmailVerificationValidator emailVerificationValidator;
	private final SocialAccountReader socialAccountReader;

	/**
	 * 이메일 기반의 신규 회원을 등록합니다.
	 * <p>
	 * 1. 기존 가입 정보(전화번호, 이름) 확인 및 상태 검증<br>
	 * 2. 이메일, 닉네임, 전화번호 중복 검사<br>
	 * 3. 비밀번호 암호화 및 신규 유저 생성 후 저장
	 *
	 * @param dto 회원가입에 필요한 정보가 담긴 DTO (이메일, 비밀번호, 이름 등)
	 * @return 가입된 사용자 정보 (토큰은 포함되지 않음)
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * 이미 존재하는 정보(이메일, 닉네임 등)가 있거나, 입력값(비밀번호 등) 형식이 유효하지 않은 경우
	 */
	@Transactional
	public AuthResult registerEmailUser(UserRegisterDto dto) {
		// 전화번호로 기존 사용자 탐색 및 사용자 상태에 따른 에러 반환
		Optional<User> userExist = userReader.checkUserExistByPhoneNumAndName(dto.phoneNumber(), dto.name());
		userExist.ifPresent(user -> userValidator.validateUserStatusForSignup(user));

		// 이메일, 닉네임, 전화번호에 대한 중복 검증 수행
		userValidator.checkEmailDuplication(dto.email());
		userValidator.checkNicknameDuplication(dto.nickname());
		userValidator.checkPhoneNumDuplication(dto.phoneNumber());
		emailVerificationValidator.validateVerified(dto.email(), dto.emailVerificationCode());
		// 신규 사용자 생성 및 검증
		User newUser = User.from(dto, passwordEncoder.encode(dto.password()));
		authValidator.validateRegisterInput(newUser, dto.password(), dto.phoneNumber());
		User savedUser = userWriter.save(newUser);
		return AuthResult.of(null, savedUser.getName(), savedUser.getEmail(), savedUser.getProfileUrl(), null);
	}

	/**
	 * 이메일과 비밀번호를 사용하여 로그인합니다.
	 *
	 * @param email    사용자 이메일
	 * @param password 사용자 비밀번호
	 * @return 액세스 토큰, 리프레시 토큰 및 사용자 기본 정보
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [INVALID_LOGIN] 비밀번호가 일치하지 않거나,
	 * [INVALID_LOGIN_USER_...] 로그인 불가능한 상태(탈퇴, 추방, 휴면)인 경우
	 */
	@Transactional
	public AuthResult loginEmailUser(String email, String password) {
		// 이메일에 대한 유저가 존재하는지 확인
		User user = userReader.findByEmailOrElseThrow(email);
		// 유효성 검증 수행 (비밀번호, 유저 상태)
		authValidator.validateCredential(user, password);
		userValidator.validateUserStatusForLogin(user);
		// 토큰 생성
		AuthTokenPair tokens = authTokenManager.issueTokens(user, null);
		return AuthResult.of(tokens.accessToken(), user.getName(), user.getEmail(), user.getProfileUrl(),
			tokens.refreshToken());
	}

	@Transactional(readOnly = true)
	public Optional<EmailFindResult> findEmail(String name, String phoneNumber) {
		Optional<User> userOptional = userReader.checkUserExistByPhoneNumAndName(phoneNumber.trim(), name.trim());
		if (userOptional.isEmpty()) {
			return Optional.empty();
		}
		// 탈퇴한 회원일 경우에도 null 처리
		User user = userOptional.get();
		if (user.isDeleted()) {
			return Optional.empty();
		}
		List<EmailFindResult.SocialAccountSummary> socialAccounts = socialAccountReader.findAllByUserId(user.getId())
			.stream()
			.sorted(Comparator.comparing(account -> account.getCreatedAt()))
			.map(account -> EmailFindResult.SocialAccountSummary.of(
				account.getSocialType().name(),
				toLocalDate(account.getCreatedAt())))
			.toList();

		if (user.isOnlySocialUser()) {
			return Optional.of(EmailFindResult.of(null, null, socialAccounts));
		}
		return Optional
			.of(EmailFindResult.of(maskEmail(user.getEmail()), toLocalDate(user.getCreatedAt()), socialAccounts));
	}

	/**
	 * 리프레시 토큰을 사용하여 액세스 토큰(및 리프레시 토큰)을 재발급합니다.
	 * <p>
	 * RTR(Refresh Token Rotation) 정책에 따라 리프레시 토큰도 함께 갱신됩니다.
	 *
	 * @param refreshToken 클라이언트(쿠키)로부터 받은 리프레시 토큰
	 * @return 갱신된 액세스 토큰과 리프레시 토큰 정보 및 사용자 기본 프로필 정보
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [REFRESH_TOKEN_MISSING] 토큰이 없거나, [INVALID_REFRESH_TOKEN] 유효하지 않은 경우
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [BLOCKED/INACTIVE_USER] 유저 상태가 활동 불가능한 경우(탈퇴 포함)
	 */
	@Transactional
	public AuthResult updateToken(String refreshToken) {
		if (refreshToken == null) {
			throw AuthErrorCode.REFRESH_TOKEN_MISSING.toBaseException();
		}
		String userId = authTokenManager.getUserIdFromRefreshToken(refreshToken);
		User user = userReader.findUserById(userId);
		userValidator.validateUser(user);
		// 토큰 생성
		AuthTokenPair tokens = authTokenManager.issueTokens(user, refreshToken);
		return AuthResult.of(tokens.accessToken(), user.getName(), user.getEmail(), user.getProfileUrl(),
			tokens.refreshToken());
	}

	/**
	 * 사용자를 로그아웃 처리합니다.
	 * <p>
	 * 1. 해당 기기의 FCM 토큰을 삭제하여 알림 수신을 중단합니다.<br>
	 * 2. 액세스 토큰을 블랙리스트에 등록하고, 리프레시 토큰을 삭제합니다.
	 *
	 * @param userId   로그아웃할 사용자 ID
	 * @param tokens   만료시킬 액세스 토큰과 리프레시 토큰 쌍
	 * @param fcmToken 삭제할 FCM 토큰 (null일 경우 생략)
	 */
	@Transactional
	public void signOut(String userId, AuthTokenPair tokens, String fcmToken) {
		if (fcmToken != null) {
			User user = userReader.findUserById(userId);
			// fcm 토큰 무효화
			userPushTokenWriter.removeFcmToken(user, fcmToken);
		}
		// jwt 토큰 무효화
		authTokenManager.invalidateTokens(tokens.accessToken(), tokens.refreshToken());
	}

	private LocalDate toLocalDate(LocalDateTime dateTime) {
		if (dateTime == null) {
			return null;
		}
		return dateTime.toLocalDate();
	}

	// 이메일 마스킹 규칙:
	// 1) '@' 앞 로컬파트의 앞 3글자(최대)를 노출한다.
	// 2) 마스킹 '*'는 최소 3개를 보장한다.
	// 3) '@' 뒤 도메인파트는 그대로 유지한다.
	// ex) "abcdef@cau.ac.kr" -> "abc***@cau.ac.kr"
	// ex) "ab@cau.ac.kr" -> "ab***@cau.ac.kr"
	private String maskEmail(String email) {
		if (email == null || !email.contains("@")) {
			return email;
		}
		String[] parts = email.split("@", 2);
		String localPart = parts[0];
		String domainPart = parts[1];

		int visibleCount = Math.min(3, localPart.length());
		String visible = localPart.substring(0, visibleCount);
		String masked = "*".repeat(Math.max(3, localPart.length() - visibleCount));
		return visible + masked + "@" + domainPart;
	}
}
