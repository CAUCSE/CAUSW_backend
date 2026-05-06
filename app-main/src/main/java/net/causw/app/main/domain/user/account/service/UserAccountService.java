package net.causw.app.main.domain.user.account.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.asset.file.service.v2.implementation.UserProfileImageReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerWriter;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserWithdrawResponse;
import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.dto.request.UserPasswordUpdateCommand;
import net.causw.app.main.domain.user.account.service.dto.result.UserMeAccountResult;
import net.causw.app.main.domain.user.account.service.dto.result.UserMeResult;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountReader;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountUnlinkManager;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoReader;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserValidator;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.domain.user.auth.service.dto.AuthResult;
import net.causw.app.main.domain.user.auth.service.dto.AuthTokenPair;
import net.causw.app.main.domain.user.auth.service.implementation.AuthTokenManager;
import net.causw.app.main.domain.user.auth.service.implementation.AuthValidator;
import net.causw.app.main.domain.user.terms.entity.Terms;
import net.causw.app.main.domain.user.terms.entity.UserTermsAgreement;
import net.causw.app.main.domain.user.terms.service.implementation.TermsReader;
import net.causw.app.main.domain.user.terms.service.implementation.TermsValidator;
import net.causw.app.main.domain.user.terms.service.implementation.UserTermsAgreementReader;
import net.causw.app.main.domain.user.terms.service.implementation.UserTermsAgreementWriter;
import net.causw.app.main.shared.dto.ProfileImageDto;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;
import net.causw.app.main.shared.infra.firebase.FcmUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAccountService {

	private final UserReader userReader;
	private final UserWriter userWriter;
	private final SocialAccountReader socialAccountReader;
	private final SocialAccountUnlinkManager socialAccountUnlinkManager;
	private final LockerReader lockerReader;
	private final LockerWriter lockerWriter;
	private final UserValidator userValidator;
	private final AuthValidator authValidator;
	private final AuthTokenManager authTokenManager;
	private final FcmUtils fcmUtils;
	private final PasswordEncoder passwordEncoder;
	private final UserProfileImageReader userProfileImageReader;
	private final TermsReader termsReader;
	private final TermsValidator termsValidator;
	private final UserTermsAgreementReader userTermsAgreementReader;
	private final UserTermsAgreementWriter userTermsAgreementWriter;
	private final UserInfoReader userInfoReader;
	private final UserProfileImageService userProfileImageService;

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
	 * @param name                  사용자의 실명(이름)
	 * @param agreedTermsIds        동의한 약관 ID 목록 (타입별 최신 필수 약관 ID 포함)
	 * @param refreshToken          갱신할 기존 리프레시 토큰
	 * @return {@link AuthResult} 변경된 권한이 반영된 새로운 토큰 세트와 유저 정보
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [INVALID_REGISTRATION_STATUS] 유저 상태가 GUEST가 아닌 경우,
	 * 닉네임 또는 전화번호가 중복된 경우,
	 * 필수 약관에 동의하지 않은 경우, 등록된 약관이 없는 경우
	 */
	@Transactional
	public AuthResult completeRegistration(String userId, String nickname, String phoneNumber, String name,
		List<String> agreedTermsIds, String refreshToken) {
		User guestUser = userReader.findUserById(userId);
		if (guestUser.getState() != UserState.GUEST) {
			throw AuthErrorCode.INVALID_REGISTRATION_STATUS.toBaseException();
		}
		List<String> distinctAgreedTermsIds = agreedTermsIds.stream().distinct().toList();
		termsValidator.validateForAgreement(distinctAgreedTermsIds);
		userValidator.checkNicknameDuplication(nickname);
		userValidator.checkPhoneNumDuplication(phoneNumber);
		guestUser.submitRegistration(name, nickname, phoneNumber);
		User updatedUser = userWriter.save(guestUser);

		List<Terms> termsToSave = termsReader.findAllById(distinctAgreedTermsIds);
		List<UserTermsAgreement> newAgreements = termsToSave.stream()
			.map(terms -> UserTermsAgreement.of(updatedUser, terms))
			.toList();
		userTermsAgreementWriter.saveAll(newAgreements);

		AuthTokenPair tokens = authTokenManager.issueTokens(updatedUser, refreshToken);

		// 신규 등록 유저는 커스텀 프로필 이미지가 없으므로 null 전달
		return AuthResult.of(tokens.accessToken(), updatedUser.getName(), updatedUser.getEmail(),
			ProfileImageDto.from(updatedUser, null),
			tokens.refreshToken(), updatedUser.isGuest(), true, updatedUser.isAcademicCertified(),
			updatedUser.getAcademicStatus());
	}

	/**
	 * 현재 로그인한 사용자의 기본 정보를 조회합니다. 내정보 메인페이지 진입 시 사용합니다.
	 *
	 * @param userId 조회할 사용자의 고유 식별자 (PK)
	 * @return {@link UserMeResult} 내 정보 결과 (이름, 닉네임, 프로필이미지, 입학년도)
	 */
	@Transactional(readOnly = true)
	public UserMeResult getMyProfile(String userId) {
		User user = userReader.findDetailById(userId);
		UserProfileImage profileImage = userProfileImageReader.findByUserIdOrNull(userId);
		boolean hasAllRequiredLatestTerms = userTermsAgreementReader.hasAgreedToAllRequiredLatestTerms(user);

		return UserMeResult.from(user, profileImage, hasAllRequiredLatestTerms);
	}

	/**
	 * 현재 로그인한 사용자의 계정 정보를 조회합니다. 내정보 > 계정 탭 진입 시 사용합니다.
	 * <p>
	 * 닉네임, 전화번호, 이메일, 온보딩 상태 등을 포함하여 반환합니다.
	 * </p>
	 *
	 * @param userId 조회할 사용자의 고유 식별자 (PK)
	 * @return {@link UserMeAccountResult} 내 계정 정보 결과 (닉네임, 전화번호, 이메일, 온보딩 상태 등)
	 */
	@Transactional(readOnly = true)
	public UserMeAccountResult getMyAccountProfile(String userId) {
		User user = userReader.findDetailById(userId);
		boolean hasAllRequiredLatestTerms = userTermsAgreementReader.hasAgreedToAllRequiredLatestTerms(user);
		var profileImage = userProfileImageReader.findByUserIdOrNull(userId);

		return UserMeAccountResult.from(user, profileImage, hasAllRequiredLatestTerms);
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

		// 관리자 강제 탈퇴(DROP) 먼저 체크
		if (user.getState().equals(UserState.DROP)) {
			throw UserErrorCode.USER_DROPPED.toBaseException();
		}

		// 일반 탈퇴 유저 체크
		if (user.isDeleted()) {
			throw UserErrorCode.USER_DELETED.toBaseException();
		}

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

	/**
	 * 탈퇴한 사용자의 계정을 복구합니다.
	 * <p>
	 * 탈퇴 후 30일 이내에만 복구가 가능합니다.
	 * </p>
	 *
	 * @param userId 복구할 사용자의 고유 식별자 (PK)
	 */
	@Transactional
	public User restore(String userId) {
		User user = userReader.findUserById(userId);

		// 30일 유예 기간 검증 (공통 로직 호출)
		userValidator.validateRestorable(user);

		return userWriter.restore(user);
	}

	/**
	 * 서비스 회원 탈퇴를 처리합니다.
	 * <p>
	 * 본 메서드는 사용자의 계정 상태를 검증하고, 탈퇴에 따른 후속 처리(Clean-up)를 수행합니다.
	 * 주요 프로세스는 다음과 같습니다:
	 * - 사용자 상태 검증 (이미 탈퇴했거나 추방된 사용자인지 확인)
	 * - 연동된 모든 소셜 계정의 외부 연동(Unlink) 및 리프레시 토큰 제거
	 * - 현재 요청에 사용된 Access/Refresh 토큰 즉시 무효화
	 * - 프로필 이미지 삭제 처리
	 * - 사용 중인 사물함이 존재할 경우 자동 반납 처리
	 * - 등록된 모든 FCM 푸시 토큰 제거
	 * - 사용자 정보 소프트 딜리트(Soft Delete) 수행
	 * </p>
	 *
	 * @param userId        탈퇴할 사용자의 식별자 (PK)
	 * @param accessToken   무효화할 현재 세션의 액세스 토큰
	 * @param refreshToken  무효화할 현재 세션의 리프레시 토큰
	 * @return {@link UserWithdrawResponse} 탈퇴 처리가 완료된 일시를 포함한 응답 객체
	 * 사용자가 이미 탈퇴했거나(USER_DELETED)
	 * 관리자에 의해 추방된 경우(USER_DROPPED)
	 */
	@Transactional
	public UserWithdrawResponse withdraw(String userId, String accessToken, String refreshToken) {
		User user = userReader.findUserById(userId);

		if (user.isDeleted()) {
			throw UserErrorCode.USER_DELETED.toBaseException();
		}

		if (user.getState() == UserState.DROP) {
			throw UserErrorCode.USER_DROPPED.toBaseException();
		}

		// 소셜 계정 unlink + provider refresh token 제거
		List<SocialAccount> socialAccounts = socialAccountReader.findAllByUserId(user.getId());
		socialAccounts.forEach(socialAccount -> {
			try {
				socialAccountUnlinkManager.unlink(socialAccount);
			} catch (RuntimeException e) {
				log.error("[User Withdraw] 소셜 연동 해제 실패. SocialType: {}, UserID: {}, Error: {}",
					socialAccount.getSocialType(), user.getId(), e.getMessage());
			}
		});

		// 현재 access / refresh token 무효화
		authTokenManager.invalidateTokens(accessToken, refreshToken);

		// 프로필 이미지 삭제
		userProfileImageService.prepareDeletionForWithdrawal(userId);

		// 부가 처리
		lockerReader.findByUserId(user.getId())
			.ifPresent(locker -> lockerWriter.returnLocker(locker, user));
		fcmUtils.clearFcmTokens(user);

		userWriter.withdraw(user);

		return UserWithdrawResponse.of(user.getDeletedAt());
	}
}
