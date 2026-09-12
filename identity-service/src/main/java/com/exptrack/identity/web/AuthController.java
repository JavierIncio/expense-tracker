package com.exptrack.identity.web;

import com.exptrack.identity.dto.LoginRequest;
import com.exptrack.identity.dto.RegisterRequest;
import com.exptrack.identity.dto.TokenResponse;
import com.exptrack.identity.exceptions.InvalidRefreshTokenException;
import com.exptrack.identity.security.CookieService;
import com.exptrack.identity.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling authentication-related endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    public AuthController(AuthService authService, CookieService cookieService) {
        this.authService = authService;
        this.cookieService = cookieService;
    }

    /**
     * Handles user registration.
     *
     * @param request  the registration request containing user details
     * @param response the HTTP response to add the refresh token cookie
     * @return a ResponseEntity containing the token response and HTTP status
     */
    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request,
                                                  HttpServletResponse response) {
        TokenResponse tokens = authService.register(request);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieService.create(tokens.refreshToken()).toString());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tokens);
    }

    /**
     * Handles user login.
     *
     * @param request  the login request containing user credentials
     * @param response the HTTP response to add the refresh token cookie
     * @return a ResponseEntity containing the token response and HTTP status
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletResponse response) {
        TokenResponse tokens = authService.login(request);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieService.create(tokens.refreshToken()).toString());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(tokens);
    }

    /**
     * Handles user logout by invalidating the refresh token and clearing the cookie.
     *
     * @param rtCookie the refresh token cookie value
     * @param response the HTTP response to clear the refresh token cookie
     * @return a ResponseEntity with HTTP status NO_CONTENT
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refresh_token") String rtCookie,
                                       HttpServletResponse response) {
        if (rtCookie != null) authService.logout(rtCookie);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieService.clear().toString());
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    /**
     * Handles token refresh by validating the refresh token and issuing new tokens.
     *
     * @param rtCookie the refresh token cookie value
     * @param response the HTTP response to add the new refresh token cookie
     * @return a ResponseEntity containing the new token response and HTTP status
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@CookieValue(name = "refresh_token") String rtCookie,
                                                 HttpServletResponse response) {
        if (rtCookie == null) throw new InvalidRefreshTokenException();
        TokenResponse tokens = authService.refresh(rtCookie);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieService.create(tokens.refreshToken()).toString());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(tokens);
    }
}
