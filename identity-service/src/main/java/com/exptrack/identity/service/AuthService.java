package com.exptrack.identity.service;

import com.exptrack.identity.domain.Role;
import com.exptrack.identity.domain.User;
import com.exptrack.identity.dto.LoginRequest;
import com.exptrack.identity.dto.RegisterRequest;
import com.exptrack.identity.dto.TokenResponse;
import com.exptrack.identity.exceptions.EmailAlreadyExistsException;
import com.exptrack.identity.exceptions.InvalidRefreshTokenException;
import com.exptrack.identity.exceptions.UsernameAlreadyExistsException;
import com.exptrack.identity.repository.UserRepository;
import com.exptrack.identity.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Service class responsible for handling authentication-related operations such as user registration,
 * token issuance, and user response conversion.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;


    public AuthService(UserRepository userRepository,
                       JwtService jwtService,
                       RefreshTokenService rtService,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = rtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registers a new user based on the provided registration request.
     *
     * @param request the registration request containing user details
     * @return a TokenResponse containing access and refresh tokens for the newly registered user
     * @throws UsernameAlreadyExistsException if the username is already taken
     * @throws EmailAlreadyExistsException if the email is already registered
     */
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username()))
            throw new UsernameAlreadyExistsException(request.username());
        if (userRepository.existsByEmail(request.email()))
            throw new EmailAlreadyExistsException(request.email());

        User user = new User(request.username(), request.email(),
                passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setRoles(Set.of(Role.USER));

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());

        userRepository.save(user);
        return issueTokens(user);
    }

    /**
     * Authenticates a user based on the provided login request and issues tokens upon successful authentication.
     *
     * @param request the login request containing user credentials
     * @return a TokenResponse containing access and refresh tokens for the authenticated user
     */
    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.login(), request.password()));
        User user = userRepository
                .findByEmailOrUsername(request.login(), request.login()) // Try to find by email first, then by username
                .orElseThrow();
        return issueTokens(user);
    }

    /**
     * Issues access and refresh tokens for the given user.
     *
     * @param user the user for whom to issue tokens
     * @return a TokenResponse containing the issued tokens
     */
    private TokenResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        refreshTokenService.store(user, refreshToken, Instant.now().plus(jwtService.getRefreshTtl()));
        return new TokenResponse(accessToken, refreshToken,
                "Bearer", jwtService.getRefreshTtl().toSeconds());
    }

    /**
     * Logs out a user by revoking the provided refresh token.
     *
     * @param refreshToken the raw refresh token to revoke
     */
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    /**
     * Refreshes the access and refresh tokens based on the provided raw refresh token.
     *
     * @param rawToken the raw refresh token to validate and use for issuing new tokens
     * @return a TokenResponse containing the newly issued access and refresh tokens
     * @throws InvalidRefreshTokenException if the provided token is invalid or revoked
     */
    public TokenResponse refresh(String rawToken) {
        Claims claims;
        try {
            claims = jwtService.parseToken(rawToken);
        } catch (JwtException e) {
            throw new InvalidRefreshTokenException();
        }

        if (!JwtService.TYPE_REFRESH.equals(claims.get("type", String.class)))
            throw new InvalidRefreshTokenException();

        if (!refreshTokenService.isValid(rawToken))
            throw new InvalidRefreshTokenException();

        User user = userRepository
                .findById(UUID.fromString(claims.getSubject()))
                .orElseThrow();

        refreshTokenService.revoke(rawToken);

        return issueTokens(user);
    }
}
