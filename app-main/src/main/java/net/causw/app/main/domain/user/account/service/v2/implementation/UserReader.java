package net.causw.app.main.domain.user.account.service.v2.implementation;

import java.util.List;
import java.util.Optional;

import net.causw.app.main.domain.user.account.repository.user.query.UserQueryRepository;
import net.causw.app.main.domain.user.account.service.dto.request.UserQueryCondition;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserReader {

    private final UserQueryRepository userQueryRepository;
	private final UserRepository userRepository;

	public Optional<User> checkUserExistByPhoneNumAndName(String phoneNum, String name) {
		return userRepository.findByPhoneNumberAndName(phoneNum, name);
	}

	public void checkEmailDuplication(String email) {
		Optional<User> emailExist = userRepository.findByEmail(email);
		if (emailExist.isPresent()) {
			throw new BaseRunTimeV2Exception(UserErrorCode.EMAIL_ALREADY_EXIST);
		}
	}

	public void checkPhoneNumDuplication(String phoneNumber) {
		Optional<User> phoneNumExist = userRepository.findByPhoneNumber(phoneNumber);
		if (phoneNumExist.isPresent()) {
			throw new BaseRunTimeV2Exception(UserErrorCode.PHONE_NUMBER_ALREADY_EXIST);
		}
	}

	public void checkNicknameDuplication(String nickname) {
		Optional<User> nicknameExist = userRepository.findByNickname(nickname);
		if (nicknameExist.isPresent()) {
			throw new BaseRunTimeV2Exception(UserErrorCode.NICKNAME_ALREADY_EXIST);
		}
	}

    public List<User> getUsersByIds(List<String> userIds) {
        return userQueryRepository.findByIds(userIds);
    }

    public List<User> searchByCondition(UserQueryCondition condition) {
        return userQueryRepository.searchByCondition(condition);
    }

}
