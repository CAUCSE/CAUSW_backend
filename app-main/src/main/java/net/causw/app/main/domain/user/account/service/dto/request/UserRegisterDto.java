package net.causw.app.main.domain.user.account.service.dto.request;

import java.util.List;

import net.causw.app.main.domain.user.auth.api.v2.dto.request.EmailSignupRequest;

public record UserRegisterDto(
	String email,
	String password,
	String name,
	String nickname,
	String phoneNumber,
	String emailVerificationCode,
	List<String> agreedTermsIds) {
	public static UserRegisterDto from(EmailSignupRequest request) {
		return new UserRegisterDto(
			request.email(),
			request.password(),
			request.name(),
			request.nickname(),
			request.phoneNumber(),
			request.emailVerificationCode(),
			request.agreedTermsIds());
	}
}
