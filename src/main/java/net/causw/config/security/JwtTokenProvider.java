package net.causw.config.security;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import net.causw.config.security.userdetails.CustomUserDetailsService;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.application.redis.auth.AuthRedisService;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.model.enums.user.UserState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    @Value("${spring.jwt.secret}")
    private String secretKey;

    private final CustomUserDetailsService userDetailsService;

    private final AuthRedisService authRedisService;

    @PostConstruct
    protected void init() {
        this.secretKey = Base64.getEncoder().encodeToString(this.secretKey.getBytes());
    }

    public String createAccessToken(String userPk, Set<Role> roles, UserState userState) {
        Date now = new Date();
        Claims claims = Jwts.claims().setSubject(userPk);
        claims.put("roles", roles.stream().map(Role::getValue).collect(Collectors.toSet()));
        claims.put("state", userState.getValue());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + StaticValue.JWT_ACCESS_TOKEN_VALID_TIME))
                .signWith(SignatureAlgorithm.HS256, this.secretKey)
                .compact();
    }

    public String createRefreshToken() {
        Date now = new Date();
        return Jwts.builder()
                .setExpiration(new Date(now.getTime() + StaticValue.JWT_REFRESH_TOKEN_VALID_TIME))
                .signWith(SignatureAlgorithm.HS256, this.secretKey)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        String userPk = getUserPk(token);
        UserDetails userDetails = userDetailsService.loadUserByUserId(userPk);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public String getUserPk(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }


    public String resolveToken(HttpServletRequest request) {
        String bearerToken =  request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    //ACCESS TOKEN만 Validate합니다.
    public boolean validateToken(String jwtToken) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(jwtToken);

            if (authRedisService.isTokenBlacklisted(jwtToken)) {
                throw new UnauthorizedException(ErrorCode.INVALID_JWT, "블랙리스트에 등록된 토큰입니다.");
            }

            return true;

        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ErrorCode.EXPIRED_JWT, MessageUtil.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException(ErrorCode.INVALID_JWT, MessageUtil.INVALID_TOKEN);
        }
    }
}
