package com.event.hub.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;


/**
 *
 */
@Component
@Slf4j
public class JwtTokenManager {
    private final SecretKey key;
    private final long expirationTime;

    public JwtTokenManager(
            @Value("${jwt.secret-key}") String key,
            @Value("${jwt.lifetime}") long expirationTime
    ) {
        byte[] decode = Decoders.BASE64.decode(key);
        this.key = Keys.hmacShaKeyFor(decode);
        this.expirationTime = expirationTime;
    }

    /**
     * Генерация токена для аутентифицированного пользователя под логином.
     * <p>
     * Как электронная подпись для этого логина на этом сервере.
     * @param login логин
     * @return токен
     */
    public String generateToken(String login) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + expirationTime);
        return Jwts.builder()
                .subject(login)
                .signWith(key)
                .issuedAt(currentDate)
                .expiration(expireDate)
                .compact();
    }

    /**
     * Получение полезной нагрузки из токена.
     * @param token токен
     * @return логин
     */
    public Claims getClaims(String token) {
        JwtParser parser = Jwts.parser()
                .verifyWith(key)
                .build();
        return parser.parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }

    }

    private boolean isTokenExpired(String token) {
        Date expiration = getClaims(token).getExpiration();
        return expiration.before(new Date());
    }
}
