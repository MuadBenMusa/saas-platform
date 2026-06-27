package com.demo.auth_code_flow.security;


import java.util.Set;

public record AuthUser(
        String subject,
        String email,
        String name,
        Set<String> roles
) {
}