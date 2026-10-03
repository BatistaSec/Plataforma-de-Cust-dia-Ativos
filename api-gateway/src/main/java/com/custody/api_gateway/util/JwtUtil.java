package com.custody.api_gateway.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    @Value("${jwt.secret:aVeryLongSecretKeyForJwtSignatureThatIsAtLeast256BitsLong123!@#}")
    private String secret;

    public void validateToken(final String token) {
        Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
