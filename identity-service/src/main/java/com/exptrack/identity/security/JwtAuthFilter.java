package com.exptrack.identity.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JwtAuthFilter is a filter that intercepts incoming HTTP requests to authenticate users based on JWT tokens.
 * It extracts the token from the "Authorization" header, validates it, and sets the authentication context accordingly.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) { this.jwtService = jwtService; }

    /**
     * Filters incoming HTTP requests to authenticate users based on JWT tokens.
     *
     * <ol>
     *     <li>Extracts the "Authorization" header from the request.</li>
     *     <li>Checks if the header starts with "Bearer " and extracts the token.</li>
     *     <li>Parses the token using the JwtService to retrieve claims.</li>
     *     <li>Validates the token type and retrieves user roles from claims.</li>
     *     <li>Creates an authentication object and sets it in the SecurityContext.</li>
     *     <li>If the token is invalid or an exception occurs, clears the SecurityContext.</li>
     *     <li>Passes the request and response to the next filter in the chain.</li>
     * </ol>
     *
     * @param request the incoming HTTP request
     * @param response the outgoing HTTP response
     * @param filterChain the filter chain to pass the request and response to the next filter
     * @throws ServletException if an error occurs during filtering
     * @throws IOException if an I/O error occurs during filtering
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                Claims claims = jwtService.parseToken(token);

                if (JwtService.TYPE_ACCESS.equals(claims.get("type", String.class))) {
                    List<SimpleGrantedAuthority> authorities = ((List<?>) claims.get("roles"))
                            .stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .toList();
                    var authentication = new UsernamePasswordAuthenticationToken(
                            claims.getSubject(), null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
