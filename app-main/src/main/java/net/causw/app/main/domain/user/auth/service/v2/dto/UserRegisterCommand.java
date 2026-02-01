package net.causw.app.main.domain.user.auth.service.v2.dto;

import net.causw.app.main.domain.user.auth.api.v2.dto.request.EmailSignupRequest;

public record UserRegisterCommand(
	String email,
	String password,
	String name,
	String nickname,
	String phoneNumber) {
	public static UserRegisterCommand from(EmailSignupRequest request) {
		return new UserRegisterCommand(
			request.email(),
			request.password(),
			request.name(),
			request.nickname(),
			request.phoneNumber());
	}
}
