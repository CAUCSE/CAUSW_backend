package net.causw.app.main.domain.user.account.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BlockedIdentifierHasher {

	private final String salt;

	public BlockedIdentifierHasher(@Value("${custom.hash.salt}") String salt) {
		this.salt = salt;
	}

	public String hash(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");

			String saltedValue = this.salt + value.trim();
			byte[] digest = md.digest(saltedValue.getBytes(StandardCharsets.UTF_8));

			StringBuilder sb = new StringBuilder();
			for (byte b : digest) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Failed to hash blocked identifier", e);
		}
	}
}
