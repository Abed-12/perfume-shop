package com.abed.perfumeshop.config.security;

public final class SecurityConstants {
    private SecurityConstants() {}

    // ========== PUBLIC ENDPOINTS ==========
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/admin/auth/**",
            "/api/customer/auth/**",
            "/api/public/**"
    };

    // ========== ROLES ==========
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    // ========== JWT RELATED ==========
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final int TOKEN_PREFIX_LENGTH = TOKEN_PREFIX.length();
    public static final String HEADER_STRING = "Authorization";

}
