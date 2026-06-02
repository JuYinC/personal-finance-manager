package com.finance.manager.security;

import com.finance.manager.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final String CLAIM_USER_ID       = "uid";
    private static final String CLAIM_TOKEN_VERSION = "tv";

    @Value("${application.jwt.secret}")
    private String secret;

    @Value("${application.jwt.expiration}")
    private Long expiration;

    // ---------------------------------------------------------------
    // Key
    // ---------------------------------------------------------------

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ---------------------------------------------------------------
    // Token generation
    // ---------------------------------------------------------------

    /**
     * Generate a JWT for the given user.
     * sub  = user UUID (immutable — never changes even if email changes)
     * uid  = user UUID (redundant but explicit for fast claim access)
     * tv   = tokenVersion (used for logout revocation)
     */
    public String generateToken(User user) {
        Date now            = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim(CLAIM_USER_ID, user.getId().toString())
                .claim(CLAIM_TOKEN_VERSION, user.getTokenVersion())
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    // ---------------------------------------------------------------
    // Claim extractors
    // ---------------------------------------------------------------

    public UUID extractUserId(String token) {
        String raw = extractClaim(token, claims -> claims.get(CLAIM_USER_ID, String.class));
        return UUID.fromString(raw);
    }

    public int extractTokenVersion(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_TOKEN_VERSION, Integer.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    // ---------------------------------------------------------------
    // Validation
    // ---------------------------------------------------------------

    /**
     * Validate that the token's embedded claims match the expected user state.
     * Does NOT hit the database — all information comes from the token itself
     * and the caller-supplied expected values.
     *
     * @param token           raw JWT string
     * @param expectedUserId  the UUID resolved from the token (already extracted by the filter)
     * @param expectedVersion the token_version currently stored for this user in the DB
     */
    public boolean validateTokenClaims(String token, UUID expectedUserId, int expectedVersion) {
        try {
            UUID    tokenUserId      = extractUserId(token);
            int     tokenVersion     = extractTokenVersion(token);
            boolean notExpired       = !extractExpiration(token).before(new Date());

            return tokenUserId.equals(expectedUserId)
                    && tokenVersion == expectedVersion
                    && notExpired;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Internal
    // ---------------------------------------------------------------

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
