package com.event.hub.security;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenManager {
    private final SecretKey key;
    private final long expirationTime;

    public JwtTokenManager(
            @Value("${jwt.secret-key}") SecretKey key,
            @Value("${jwt.lifetime}") long expirationTime
    ) {
        this.key = key;
        this.expirationTime = expirationTime;
    }

    public String generateToken(String login) {
        return Jwts.builder()
                .subject(login)
                .signWith(key)
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .issuedAt(new Date())
                .compact();
    }

    public String getLoginFromToken(String token) {
        JwtParser parser = Jwts.parser()
                .verifyWith(key)
                .build();
        return parser.parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
