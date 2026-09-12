package com.exptrack.identity.security;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Service for creating and clearing HTTP cookies related to authentication tokens.
 */
@Component
public class CookieService {

    private final JwtService jwtService;

    public CookieService(JwtService jwtService) {this.jwtService = jwtService;}

    /**
     * Creates a new HTTP cookie for the provided refresh token.
     *
     * @param refreshToken the refresh token to be stored in the cookie
     * @return a ResponseCookie object representing the created cookie
     */
    public ResponseCookie create(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(jwtService.getRefreshTtl().getSeconds())
                .build();
    }

    /**
     * Clears the refresh token cookie by setting its value to an empty string and max age to 0.
     *
     * @return a ResponseCookie object representing the cleared cookie
     */
    public ResponseCookie clear() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }
}
