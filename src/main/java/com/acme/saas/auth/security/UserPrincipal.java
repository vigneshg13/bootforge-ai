package com.acme.saas.auth.security;

import java.util.UUID;

public record UserPrincipal(UUID userId, String email) {}
