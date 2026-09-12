package com.exptrack.identity.service;

import com.exptrack.identity.domain.RefreshToken;
import com.exptrack.identity.domain.User;
import com.exptrack.identity.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Stores a new refresh token for the given user.
     *
     * @param user       the user to associate with the refresh token
     * @param rawToken   the raw refresh token string
     * @param expiration the expiration time of the refresh token
     */
    public void store(User user, String rawToken, Instant expiration) {
        RefreshToken rt = new RefreshToken();
        rt.setUserId(user.getId());
        rt.setTokenHash(hash(rawToken));
        rt.setExpiresAt(expiration);
        rt.setRevoked(false);
        rt.setCreatedAt(Instant.now());
        refreshTokenRepository.save(rt);
    }

    /**
     * Checks if a refresh token is valid.
     *
     * @param rawToken the raw refresh token string
     * @return true if the token is valid, false otherwise
     */
    public boolean isValid(String rawToken) {
        return refreshTokenRepository.findByTokenHash(hash(rawToken))
                .map(rt -> !rt.isRevoked() && rt.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
    }

    /**
     * Revokes a refresh token, marking it as no longer valid.
     *
     * @param rawToken the raw refresh token string to revoke
     */
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken))
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
    }

    /**
     * Hashes the raw token using SHA-256 and returns the hexadecimal representation.
     *
     * @param rawToken the raw token string to hash
     * @return the hashed token as a hexadecimal string
     */
    private String hash(String rawToken) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }


}
