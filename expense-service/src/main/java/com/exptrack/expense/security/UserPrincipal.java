package com.exptrack.expense.security;

import java.util.List;
import java.util.UUID;

public record UserPrincipal(UUID userId, String email, List<String> roles) {}
