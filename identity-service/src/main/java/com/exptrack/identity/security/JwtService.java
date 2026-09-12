package com.exptrack.identity.security;

import com.exptrack.identity.domain.Role;
import com.exptrack.identity.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

/**
 * Service for generating and parsing JSON Web Tokens (JWTs) for user authentication.
 */
@Service
public class JwtService {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final String issuer;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    /**
     * Constructs a JwtService with the specified secret key, issuer, and token time-to-live (TTL) durations.
     *
     * @param secret      the secret key used for signing JWTs
     * @param issuer      the issuer of the JWTs
     * @param accessTtl   the time-to-live duration for access tokens
     * @param refreshTtl  the time-to-live duration for refresh tokens
     */
    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.issuer}") String issuer,
                      @Value("${app.jwt.access-token-ttl}") Duration accessTtl,
                      @Value("${app.jwt.refresh-token-ttl}") Duration refreshTtl) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.issuer = issuer;
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
    }

    /**
     * Generates an access token for the given user.
     *
     * @param user the user for whom the access token is being generated
     * @return a signed JWT access token as a String
     */
    public String generateAccessToken(User user) {
        return buildToken(user, TYPE_ACCESS, accessTtl);
    }

    /**
     * Generates a refresh token for the given user.
     *
     * @param user the user for whom the refresh token is being generated
     * @return a signed JWT refresh token as a String
     */
    public String generateRefreshToken(User user) {
        return buildToken(user, TYPE_REFRESH, refreshTtl);
    }

    /**
     * Returns the time-to-live (TTL) for access tokens.
     *
     * @return the TTL for access tokens
     */
    public Duration getAccessTtl() {return accessTtl;}

    /**
     * Returns the time-to-live (TTL) for refresh tokens.
     *
     * @return the TTL for refresh tokens
     */
    public Duration getRefreshTtl() {return refreshTtl;}

    /**
     * Parses the given JWT token and returns the claims contained within it.
     *
     * @param token the JWT token to parse
     * @return the claims extracted from the token
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Builds a JWT token for the given user with the specified type and time-to-live (TTL).
     *
     * @param user the user for whom the token is being generated
     * @param type the type of token (e.g., "access" or "refresh")
     * @param ttl  the time-to-live duration for the token
     * @return a signed JWT token as a String
     */
    private String buildToken(User user, String type, Duration ttl) {
        Date now = new Date();
        return io.jsonwebtoken.Jwts.builder()
                .subject(user.getId().toString())
                .claim("type", type)
                .claim("email", user.getEmail())
                .claim("roles", user.getRoles().stream().map(Enum::name).toList())
                .claim("jti", UUID.randomUUID().toString())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttl.toMillis()))
                .signWith(key)
                .compact();
    }
}
