package com.exptrack.identity.config;

import com.exptrack.identity.repository.UserRepository;
import com.exptrack.identity.security.JwtAuthFilter;
import com.exptrack.identity.security.JwtService;
import com.exptrack.identity.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration class for the application.
 * It defines beans for password encoding, user details service, authentication provider,
 * and authentication manager.
 */
@Configuration
public class SecurityConfig {

    /**
     * Configures the password encoder to be used for hashing passwords.
     *
     * @return a PasswordEncoder instance using BCrypt hashing algorithm
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the UserDetailsService to load user details from the UserRepository.
     * It allows users to authenticate using either their email or username.
     *
     * @param repository the UserRepository used to fetch user data
     * @return a UserDetailsService instance for authentication
     */
    @Bean
    UserDetailsService userDetailsService(UserRepository repository) {
        return identifier -> repository.findByEmailOrUsername(identifier, identifier)
                .map(user -> User
                        .withUsername(user.getEmail())
                        .password(user.getPasswordHash())
                        .disabled(!user.isEnabled())
                        .authorities(user.getRoles().stream()
                                .map(role -> "ROLE_" + role.name())
                                .toArray(String[]::new))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(identifier));
    }

    /**
     * Configures the DaoAuthenticationProvider to use the custom UserDetailsService and PasswordEncoder.
     *
     * @param userDetailsService the UserDetailsService for loading user details
     * @param passwordEncoder    the PasswordEncoder for hashing passwords
     * @return a DaoAuthenticationProvider instance for authentication
     */
    @Bean
    DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                     PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Configures the AuthenticationManager to be used for authentication.
     *
     * @param configuration the AuthenticationConfiguration used to obtain the AuthenticationManager
     * @return an AuthenticationManager instance for handling authentication requests
     * @throws Exception if an error occurs while obtaining the AuthenticationManager
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Configures the security filter chain for the application.
     * It defines the security settings, including CSRF protection, session management,
     * exception handling, and request authorization.
     *
     * <ol>
     *     <li>Disables CSRF protection.</li>
     *     <li>Configures session management to be stateless.</li>
     *     <li>Configures exception handling to use the provided RestAuthenticationEntryPoint.</li>
     *     <li>Permits access to the specified endpoints without authentication.</li>
     *     <li>Requires authentication for all other requests.</li>
     *     <li>Adds the JwtAuthFilter before the UsernamePasswordAuthenticationFilter.</li>
     * </ol>
     *
     * @param http          the HttpSecurity object used to configure security settings
     * @param entryPoint    the RestAuthenticationEntryPoint for handling authentication exceptions
     * @param jwtAuthFilter the JwtAuthFilter for processing JWT tokens in requests
     * @return a SecurityFilterChain instance for securing HTTP requests
     * @throws Exception if an error occurs while configuring the security filter chain
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    RestAuthenticationEntryPoint entryPoint,
                                    JwtAuthFilter jwtAuthFilter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(entryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/logout"
                        ).permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(
                                "/actuator/health"
                        ).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
