package com.event.security;

import com.event.domain.UserDto;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class JwtTokenGenerator {
    private final SecretKey key;
    private final long expirationTime;

    public JwtTokenGenerator(
            @Value("${jwt.secret-key}") String key,
            @Value("${jwt.lifetime}") long expirationTime
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
        this.expirationTime = TimeUnit.MINUTES.toMillis(expirationTime);
    }

    /**
     * Генерация токена для аутентифицированного пользователя под логином.
     * <p>
     * Как электронная подпись для этого логина на этом сервере.
     *
     * @param userDto user
     * @return токен
     */
    public String generateToken(UserDto userDto) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + expirationTime);

        return Jwts.builder()
                .subject(userDto.getSubject())
                .claims(userDto.getClaims())
                .signWith(key)
                .issuedAt(currentDate)
                .expiration(expireDate)
                .compact();
    }
}
