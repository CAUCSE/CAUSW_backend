package net.causw.app.main.domain.user.auth.service.v2.implementation;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.domain.user.account.util.PasswordFormatValidator;
import net.causw.app.main.domain.user.account.util.PhoneNumberFormatValidator;
import net.causw.app.main.domain.user.auth.api.v2.dto.AuthDtoMapper;
import net.causw.app.main.domain.user.auth.api.v2.dto.response.AuthResponse;
import net.causw.app.main.domain.user.auth.service.v2.dto.UserRegisterCommand;
import net.causw.app.main.shared.ValidatorBucket;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;
import net.causw.app.main.shared.util.ConstraintValidator;

import jakarta.transaction.Transactional;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterUserUseCase {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final Validator validator;

	public AuthResponse execute(UserRegisterCommand command) {

		//기존 사용자 탐색 (이름 + 전화번호)
		Optional<User> userExist = userRepository.findByPhoneNumberAndName(command.phoneNumber(), command.name());
		userExist.ifPresent(User::validateSignUpPossible);

		//사용자 정보 중복 체크
		//TODO: 사용자 정책 확정 이후 검증 로직 수정
		Optional<User> emailExist = userRepository.findByEmail(command.email());
		if (emailExist.isPresent()) {
			throw new BaseRunTimeV2Exception(UserErrorCode.EMAIL_ALREADY_EXIST);
		}
		Optional<User> phoneNumExist = userRepository.findByPhoneNumber(command.phoneNumber());
		if (phoneNumExist.isPresent()) {
			throw new BaseRunTimeV2Exception(UserErrorCode.PHONE_NUMBER_ALREADY_EXIST);
		}
		Optional<User> nicknameExist = userRepository.findByNickname(command.nickname());
		if (nicknameExist.isPresent()) {
			throw new BaseRunTimeV2Exception(UserErrorCode.NICKNAME_ALREADY_EXIST);
		}

		//신규 사용자 생성
		User newUser = User.from(command, passwordEncoder.encode(command.password()));
		validateUser(command, newUser);
		userRepository.save(newUser);
		return AuthDtoMapper.INSTANCE.toAuthResponse(newUser, null, null);
	}

	private void validateUser(UserRegisterCommand command, User user) {
		ValidatorBucket.of()
			.consistOf(ConstraintValidator.of(user, this.validator))
			.consistOf(PasswordFormatValidator.of(command.password()))
			.consistOf(PhoneNumberFormatValidator.of(command.phoneNumber()))
			.validate();
	}
}
