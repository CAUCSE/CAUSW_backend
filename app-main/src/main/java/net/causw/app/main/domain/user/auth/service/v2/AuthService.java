package net.causw.app.main.domain.user.auth.service.v2;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.v2.dto.UserRegisterDto;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserValidator;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserWriter;
import net.causw.app.main.domain.user.auth.api.v2.dto.AuthDtoMapper;
import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;
import net.causw.app.main.domain.user.auth.service.v2.dto.AuthResult;
import net.causw.app.main.domain.user.auth.service.v2.dto.AuthTokenPair;
import net.causw.app.main.domain.user.auth.service.v2.implementation.AuthTokenManager;
import net.causw.app.main.domain.user.auth.service.v2.implementation.AuthValidator;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserReader userReader;
	private final UserWriter userWriter;
	private final PasswordEncoder passwordEncoder;
	private final AuthDtoMapper authDtoMapper;
	private final UserValidator userValidator;
	private final AuthValidator authValidator;
	private final AuthTokenManager authTokenManager;

	@Transactional
	public AuthResponse registerEmailUser(UserRegisterDto dto) {
		// 전화번호로 기존 사용자 탐색 및 사용자 상태에 따른 에러 반환
		Optional<User> userExist = userReader.checkUserExistByPhoneNumAndName(dto.phoneNumber(), dto.name());
		userExist.ifPresent(user -> userValidator.validateUserStatusForSignup(user.getState()));

		// 이메일, 닉네임, 전화번호에 대한 중복 검증 수행
		userValidator.checkEmailDuplication(dto.email());
		userValidator.checkNicknameDuplication(dto.nickname());
		userValidator.checkPhoneNumDuplication(dto.phoneNumber());

		// 신규 사용자 생성 및 검증
		User newUser = User.from(dto, passwordEncoder.encode(dto.password()));
		authValidator.validateRegisterInput(newUser, dto.password(), dto.phoneNumber());
		User savedUser = userWriter.save(newUser);
		return authDtoMapper.toAuthResponse(savedUser, null, savedUser.getProfileUrl());
	}

	@Transactional
	public AuthResult loginEmailUser(String email, String password) {
		// 이메일에 대한 유저가 존재하는지 확인
		User user = userReader.findByEmailOrElseThrow(email);
		// 유효성 검증 수행 (비밀번호, 유저 상태)
		authValidator.validateCredential(user, password);
		// 토큰 생성
		AuthTokenPair tokens = authTokenManager.issueTokens(user);
		return AuthResult.of(authDtoMapper.toAuthResponse(user, tokens.accessToken(), user.getProfileUrl()),
			tokens.refreshToken());
	}
}
