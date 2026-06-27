package com.demo.auth_code_flow.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CurrentUserService {

    public AuthUser from(OAuth2AuthenticationToken token) {
        if (token == null || token.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated user found");
        }

        String subject = token.getPrincipal().getAttribute("sub");
        String email = token.getPrincipal().getAttribute("email");
        String name = token.getPrincipal().getAttribute("name");

        Set<String> roles = token.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return new AuthUser(subject, email, name, roles);
    }
}