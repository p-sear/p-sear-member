package com.pser.member.config;

import com.pser.member.Util;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TokenProvider {
    private final Key key;
    private final Long tokenValidityInSeconds;

    public TokenProvider(Environment env) {
        String secret = env.getProperty("jwt.secret");
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.tokenValidityInSeconds = Util.getLongProperty(env, "jwt.token-validity-in-seconds");
    }

    public String createToken(String email, boolean isRefresh) {
        return Jwts.builder()
                .setSubject(email)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(getExpiration(isRefresh))
                .setIssuedAt(new Date())
                .compact();
    }

    public String createToken(Authentication authentication, boolean isRefresh) {
        return createToken(authentication.getName(), isRefresh);
    }

    public String refresh(String refreshToken) {
        Claims claims = getClaims(refreshToken);
        return createToken(claims.getSubject(), true);
    }

    private Claims getClaims(String token) {
        Claims claims;
        try {
            JwtParser jwtParser = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build();
            claims = jwtParser
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SecurityException | MalformedJwtException e) {
            throw new MalformedJwtException("잘못된 JWT 서명입니다");
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "만료된 JWT 토큰입니다");
        } catch (UnsupportedJwtException e) {
            throw new UnsupportedJwtException("지원되지 않는 JWT 토큰입니다");
        } catch (IllegalArgumentException e) {
            throw new MalformedJwtException("JWT 토큰이 잘못되었습니다");
        }
        return claims;
    }

    private Date getExpiration(boolean isRefresh) {
        long nowMilliSeconds = new Date().getTime();
        long tokenValidityInMilliSeconds = tokenValidityInSeconds * 1000;
        if (isRefresh) {
            tokenValidityInMilliSeconds *= 30;
        }
        long expirationMilliSeconds = nowMilliSeconds + tokenValidityInMilliSeconds;
        return new Date(expirationMilliSeconds);
    }
}
