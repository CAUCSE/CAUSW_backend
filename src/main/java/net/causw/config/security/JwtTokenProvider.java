package net.causw.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import net.causw.domain.exceptions.ErrorCode;
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

    public String createToken(String userPk, Role role, UserState userState) {
        Claims claims = Jwts.claims().setSubject(userPk);
        claims.put("role", role.getValue());
        claims.put("state", userState.getValue());

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + StaticValue.JWT_TOKEN_VALID_TIME))
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

    public boolean validateToken(String jwtToken, HttpServletRequest request) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(jwtToken);

            if (claims.getBody().getExpiration().before(new Date())) {
                request.setAttribute("exception", ErrorCode.INVALID_JWT);
                return false;
            }

            if (claims.getBody().get("role").equals(Role.NONE.getValue()) ||
                    !claims.getBody().get("state").equals(UserState.ACTIVE.getValue())) {
                request.setAttribute("exception", ErrorCode.NEED_SIGN_IN);
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
