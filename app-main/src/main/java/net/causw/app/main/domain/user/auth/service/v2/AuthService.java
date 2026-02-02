package net.causw.app.main.domain.user.auth.service.v2;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.v2.dto.UserRegisterDto;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserWriter;
import net.causw.app.main.domain.user.account.util.PasswordFormatValidator;
import net.causw.app.main.domain.user.account.util.PhoneNumberFormatValidator;
import net.causw.app.main.domain.user.auth.api.v2.dto.AuthDtoMapper;
import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;
import net.causw.app.main.shared.ValidatorBucket;
import net.causw.app.main.shared.util.ConstraintValidator;

import jakarta.transaction.Transactional;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserReader userReader;
	private final UserWriter userWriter;
	private final Validator validator;
	private final PasswordEncoder passwordEncoder;
	private final AuthDtoMapper authDtoMapper;

	@Transactional
	public AuthResponse registerEmailUser(UserRegisterDto dto) {
		// 전화번호로 기존 사용자 탐색 및 사용자 상태에 따른 에러 반환
		Optional<User> userExist = userReader.checkUserExistByPhoneNumAndName(dto.phoneNumber(), dto.name());
		userExist.ifPresent(User::validateSignUpPossible);

		// 이메일, 닉네임, 전화번호에 대한 중복 검증 수행
		userReader.checkEmailDuplication(dto.email());
		userReader.checkNicknameDuplication(dto.nickname());
		userReader.checkPhoneNumDuplication(dto.phoneNumber());

		// 신규 사용자 생성
		User newUser = User.from(dto, passwordEncoder.encode(dto.password()));
		validateEmailUser(dto, newUser);
		userWriter.save(newUser);
		return authDtoMapper.toAuthResponse(newUser, null, null);
	}

	private void validateEmailUser(UserRegisterDto dto, User user) {
		ValidatorBucket.of()
			.consistOf(ConstraintValidator.of(user, this.validator))
			.consistOf(PasswordFormatValidator.of(dto.password()))
			.consistOf(PhoneNumberFormatValidator.of(dto.phoneNumber()))
			.validate();
	}
}
