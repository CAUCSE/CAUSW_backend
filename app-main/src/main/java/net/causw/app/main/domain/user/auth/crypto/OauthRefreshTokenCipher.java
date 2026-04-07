package net.causw.app.main.domain.user.auth.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * OAuth 리프레시 토큰을 AES-256-GCM으로 암·복호화합니다.
 * <p>
 * {@code app.oauth.refresh-token.aes-key-base64}에 URL-safe Base64로 인코딩된 32바이트 키를 설정해야 합니다.
 */
@Component
public class OauthRefreshTokenCipher {

	private static final String TRANSFORMATION = "AES/GCM/NoPadding";
	private static final int GCM_IV_LENGTH = 12;
	private static final int GCM_TAG_BITS = 128;
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private final SecretKeySpec secretKeySpec;

	public OauthRefreshTokenCipher(@Value("${app.oauth.refresh-token.aes-key-base64:}") String keyBase64) {
		if (!StringUtils.hasText(keyBase64)) {
			throw new IllegalStateException(
				"app.oauth.refresh-token.aes-key-base64 must be set (32-byte key, Base64-encoded)");
		}
		byte[] keyBytes = Base64.getDecoder().decode(keyBase64.trim());
		if (keyBytes.length != 32) {
			throw new IllegalArgumentException("app.oauth.refresh-token.aes-key-base64 must decode to 32 bytes");
		}
		this.secretKeySpec = new SecretKeySpec(keyBytes, "AES");
	}

	public String encrypt(String plaintext) {
		if (!StringUtils.hasText(plaintext)) {
			throw new IllegalArgumentException("OAuth refresh token plaintext must not be blank");
		}
		try {
			byte[] iv = new byte[GCM_IV_LENGTH];
			SECURE_RANDOM.nextBytes(iv);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
			byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
			ByteBuffer buf = ByteBuffer.allocate(iv.length + cipherText.length);
			buf.put(iv);
			buf.put(cipherText);
			return Base64.getEncoder().encodeToString(buf.array());
		} catch (Exception e) {
			throw new IllegalStateException("Failed to encrypt OAuth refresh token", e);
		}
	}

	public String decrypt(String cipherBase64) {
		if (!StringUtils.hasText(cipherBase64)) {
			return null;
		}
		try {
			byte[] decoded = Base64.getDecoder().decode(cipherBase64.trim());
			if (decoded.length < GCM_IV_LENGTH + 1) {
				throw new IllegalArgumentException("Invalid cipher payload");
			}
			ByteBuffer buf = ByteBuffer.wrap(decoded);
			byte[] iv = new byte[GCM_IV_LENGTH];
			buf.get(iv);
			byte[] cipherBytes = new byte[buf.remaining()];
			buf.get(cipherBytes);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
			byte[] plain = cipher.doFinal(cipherBytes);
			return new String(plain, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to decrypt OAuth refresh token", e);
		}
	}
}
