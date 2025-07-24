package net.causw.app.main.service.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PasswordGenerator {
	@Value("${spring.password.prefix}")
	private String PASSWORD_PREFIX;

	public String generate() {
		return PASSWORD_PREFIX + (int)Math.floor(Math.random() * 10000000 + 1000000);
	}
}
