package com.exptrack.gateway.config;

import com.exptrack.gateway.security.JwtAuthFilter;
import com.exptrack.gateway.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the application.
 */
@Configuration
public class SecurityConfig {

    /**
     * Configures the security filter chain for the application.
     *
     * <ol>
     *     <li>Disables CSRF protection.</li>
     *     <li>Sets the session management policy to stateless.</li>
     *     <li>Configures the authentication entrypoint for handling authentication exceptions.</li>
     *     <li>Authorizes requests to the specified endpoints and requires authentication for all other requests.</li>
     *     <li>Adds the JWT authentication filter before the UsernamePasswordAuthenticationFilter.</li>
     * </ol>
     *
     * @param http       the HttpSecurity to configure
     * @param entryPoint the RestAuthenticationEntryPoint for handling authentication exceptions
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs while configuring the security filter chain
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    RestAuthenticationEntryPoint entryPoint,
                                    JwtAuthFilter jwtAuthFilter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh.authenticationEntryPoint(entryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/logout"
                        ).permitAll()
                        .requestMatchers(
                                "/actuator/health"
                        ).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
