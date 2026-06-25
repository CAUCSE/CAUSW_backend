package net.causw.app.main.core.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetailsService;
import net.causw.app.main.shared.infra.redis.RedisUtils;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.UnauthorizedException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

	@Value("${spring.jwt.secret}")
	private String secret;

	private SecretKey secretKey;

	private final CustomUserDetailsService userDetailsService;
	private final RedisUtils redisUtils;

	@PostConstruct
	protected void init() {
		this.secretKey = new SecretKeySpec(this.secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}

	public String createAccessToken(String userPk, Set<Role> roles, UserState userState) {
		Date now = new Date();

		return Jwts.builder()
			.subject(userPk)
			.claim("roles", roles.stream().map(Role::getValue).collect(Collectors.toSet()))
			.claim("state", userState.getValue())
			.issuedAt(now)
			.expiration(new Date(now.getTime() + StaticValue.JWT_ACCESS_TOKEN_VALID_TIME))
			.signWith(secretKey, Jwts.SIG.HS256)
			.compact();
	}

	public String createRefreshToken() {
		Date now = new Date();

		return Jwts.builder()
			.expiration(new Date(now.getTime() + StaticValue.JWT_REFRESH_TOKEN_VALID_TIME))
			.signWith(secretKey, Jwts.SIG.HS256)
			.compact();
	}

	public Authentication getAuthentication(String token) {
		String userPk = getUserPk(token);
		UserDetails userDetails = userDetailsService.loadUserByUserId(userPk);

		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}

	public String getUserPk(String token) {
		return parseClaims(token).getSubject();
	}

	/**
	 * @deprecated JWT 의존성 분리를 위해 사용이 중단되었습니다.
	 * 대신 {@link net.causw.app.main.shared.util.AuthorizationExtractor#extract(HttpServletRequest)}를 사용해 주세요.
	 */
	@Deprecated
	public String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");

		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}

		return null;
	}

	// ACCESS TOKEN만 Validate합니다.
	public boolean validateToken(String jwtToken) {
		try {
			parseClaims(jwtToken);

			if (redisUtils.isTokenBlacklisted(jwtToken)) {
				throw new UnauthorizedException(ErrorCode.INVALID_JWT, "블랙리스트에 등록된 토큰입니다.");
			}

			return true;
		} catch (ExpiredJwtException e) {
			throw new UnauthorizedException(ErrorCode.EXPIRED_JWT, MessageUtil.EXPIRED_TOKEN);
		} catch (JwtException | IllegalArgumentException e) {
			throw new UnauthorizedException(ErrorCode.INVALID_JWT, MessageUtil.INVALID_TOKEN);
		}
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
}
