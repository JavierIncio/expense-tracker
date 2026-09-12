package com.exptrack.gateway.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * A wrapper for HttpServletRequest that adds user information headers.
 *
 * <p>This class extends HttpServletRequestWrapper and overrides methods to provide
 * additional headers containing user information such as user ID, email, and roles.
 * It is typically used in conjunction with a filter that extracts user information
 * from a JWT token and wraps the original request.</p>
 */
public class UserHeadersRequestWrapper extends HttpServletRequestWrapper {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_EMAIL_HEADER = "X-User-Email";
    public static final String USER_ROLES_HEADER = "X-User-Roles";

    private final String userId;
    private final String userEmail;
    private final String userRoles;

    public UserHeadersRequestWrapper(HttpServletRequest request,
                                     String userId, String userEmail, String userRoles) {
        super(request);
        this.userId = userId;
        this.userEmail = userEmail;
        this.userRoles = userRoles;
    }

    /**
     * Override the getHeader method to return the user information headers.
     *
     * <p>If the requested header is one of the user information headers, return the corresponding value.
     * Otherwise, delegate to the superclass implementation.</p>
     */
    @Override
    public String getHeader(String name) {
        if (USER_ID_HEADER.equalsIgnoreCase(name)) return this.userId;
        if (USER_EMAIL_HEADER.equalsIgnoreCase(name)) return this.userEmail;
        if (USER_ROLES_HEADER.equalsIgnoreCase(name)) return this.userRoles;

        return super.getHeader(name);
    }

    /**
     * Override the getHeaders method to return an enumeration of header values for the user information headers.
     *
     * <p>If the requested header is one of the user information headers, return an enumeration
     * containing the corresponding value. Otherwise, delegate to the superclass implementation.</p>
     */
    @Override
    public Enumeration<String> getHeaders(String name) {
        if (USER_ID_HEADER.equalsIgnoreCase(name)
                || USER_EMAIL_HEADER.equalsIgnoreCase(name)
                || USER_ROLES_HEADER.equalsIgnoreCase(name)) {
            String value = getHeader(name);
            return value != null
                    ? Collections.enumeration(List.of(value))
                    : Collections.emptyEnumeration();
        }
        return super.getHeaders(name);
    }

    /**
     * Override the getHeaderNames method to return an enumeration of all header names,
     * including user information headers.
     *
     * <p>This method removes any existing user information headers from the list of header
     * names and adds them back if they are not null.</p>
     */
    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> headerNames = Collections.list(super.getHeaderNames());
        headerNames.removeIf(name -> USER_ID_HEADER.equalsIgnoreCase(name)
                || USER_EMAIL_HEADER.equalsIgnoreCase(name)
                || USER_ROLES_HEADER.equalsIgnoreCase(name));

        if (userId != null) headerNames.add(USER_ID_HEADER);
        if (userEmail != null) headerNames.add(USER_EMAIL_HEADER);
        if (userRoles != null) headerNames.add(USER_ROLES_HEADER);

        return Collections.enumeration(headerNames);
    }
}
