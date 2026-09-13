package com.exptrack.expense.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class XUserAuthFilterTest {

    static final UUID USER_ID = UUID.randomUUID();

    final XUserAuthFilter filter = new XUserAuthFilter();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noUserIdHeader_keepsContextEmpty() throws Exception {
        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void blankUserIdHeader_keepsContextEmpty() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "   ");
        filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void invalidUuid_keepsContextEmpty() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "not-a-uuid");
        filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void validHeaders_setsAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", USER_ID.toString());
        request.addHeader("X-User-Email", "user@example.com");
        request.addHeader("X-User-Roles", "USER,ADMIN");

        filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isInstanceOf(UserPrincipal.class);
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        assertThat(principal.userId()).isEqualTo(USER_ID);
        assertThat(principal.email()).isEqualTo("user@example.com");
        assertThat(principal.roles()).containsExactly("USER", "ADMIN");
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void blankRolesHeader_setsEmptyAuthorities() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", USER_ID.toString());
        request.addHeader("X-User-Email", "user@example.com");

        filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isInstanceOf(UserPrincipal.class);
        assertThat(auth.getAuthorities()).isEmpty();
    }
}