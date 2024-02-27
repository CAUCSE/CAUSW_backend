package net.causw.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.model.enums.UserState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    @Value("${spring.jwt.secret}")
    private String secretKey;

    @PostConstruct
    protected void init() {
        this.secretKey = Base64.getEncoder().encodeToString(this.secretKey.getBytes());
    }

    public String createAccessToken(String userPk, Role role, UserState userState) {
        Date now = new Date();
        Claims claims = Jwts.claims().setSubject(userPk);
        claims.put("role", role.getValue());
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
        String userPk = Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody().getSubject();
        return new UsernamePasswordAuthenticationToken(userPk, null, new ArrayList<>());
    }

    public String resolveToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    public boolean validateToken(String jwtToken) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(jwtToken);

            if (claims.getBody().getExpiration().before(new Date())) {
                throw new UnauthorizedException(ErrorCode.INVALID_JWT, "만료된 토큰입니다.");
            }

            if (claims.getBody().get("role").equals(Role.NONE.getValue()) ||
                    !claims.getBody().get("state").equals(UserState.ACTIVE.getValue())) {
                throw new BadRequestException(ErrorCode.NEED_SIGN_IN, "다시 로그인 하세요.");
            }
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
