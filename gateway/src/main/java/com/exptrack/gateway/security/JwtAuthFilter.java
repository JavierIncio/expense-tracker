package com.exptrack.gateway.security;

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
 * A filter that authenticates incoming HTTP requests based on JWT tokens.
 * <p>
 * This filter extracts the JWT token from the "Authorization" header of the request,
 * validates it, and sets the authentication context for the request if the token is valid.
 * It also wraps the request to include user information in headers for downstream processing.
 * </p>
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {this.jwtService = jwtService;}

    /**
     * Filters incoming HTTP requests to authenticate users based on JWT tokens.
     *
     * @param request     the HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if an error occurs during filtering
     * @throws IOException      if an I/O error occurs during filtering
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userId     = null;
        String userEmail  = null;
        String userRoles  = null;

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                Claims claims = jwtService.parseToken(token);

                if (JwtService.TYPE_ACCESS.equals(claims.get("type", String.class))) {
                    List<?> roles = (List<?>) claims.get("roles");

                    if (roles != null && !roles.isEmpty()) {
                        userId    = String.valueOf(claims.getSubject());
                        userEmail = claims.get("email", String.class);
                        userRoles = roles.stream()
                                .map(String::valueOf)
                                .collect(java.util.stream.Collectors.joining(","));

                        List<SimpleGrantedAuthority> authorities = roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .toList();

                        var authentication = new UsernamePasswordAuthenticationToken(
                                claims.getSubject(), null, authorities);

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
            }
        }
        UserHeadersRequestWrapper wrapped = new UserHeadersRequestWrapper(request, userId, userEmail, userRoles);
        filterChain.doFilter(wrapped, response);
    }
}
