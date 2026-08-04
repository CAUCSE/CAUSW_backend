package net.causw.app.main.core.security;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetailsService;
import net.causw.app.main.shared.infra.redis.RedisUtils;

import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

	private static final String SECRET = "test-secret-key-for-jwt-token-provider-unit-test";

	@Mock
	private CustomUserDetailsService userDetailsService;

	@Mock
	private RedisUtils redisUtils;

	private JwtTokenProvider jwtTokenProvider;

	@BeforeEach
	void setUp() {
		jwtTokenProvider = new JwtTokenProvider(userDetailsService, redisUtils);
		ReflectionTestUtils.setField(jwtTokenProvider, "secret", SECRET);
		jwtTokenProvider.init();
	}

	@Test
	@DisplayName("같은 초에 연속 발급해도 리프레시 토큰 문자열은 서로 겹치지 않는다")
	void createRefreshToken_neverCollides() {
		int issueCount = 1000;

		Set<String> refreshTokens = new HashSet<>();
		for (int i = 0; i < issueCount; i++) {
			refreshTokens.add(jwtTokenProvider.createRefreshToken());
		}

		assertEquals(issueCount, refreshTokens.size());
	}

	@Test
	@DisplayName("리프레시 토큰은 jti를 가지며 사용자 식별 정보를 담지 않는다")
	void createRefreshToken_hasJtiWithoutSubject() {
		String refreshToken = jwtTokenProvider.createRefreshToken();

		var claims = Jwts.parser()
			.verifyWith(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
			.build()
			.parseSignedClaims(refreshToken)
			.getPayload();

		assertNotNull(claims.getId());
		assertNull(claims.getSubject());
	}
}
