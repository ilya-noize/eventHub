package com.event.hub.security;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;


/**
 *
 */
@Component
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
     * Получение логина из токена.
     * <p>
     * Как подтверждение принадлежности логина к этой электронной подписи.
     * @param token токен
     * @return логин
     */
    public String getLoginFromToken(String token) {
        JwtParser parser = Jwts.parser()
                .verifyWith(key)
                .build();
        return parser.parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
