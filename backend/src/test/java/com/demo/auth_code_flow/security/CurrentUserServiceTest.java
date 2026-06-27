package com.demo.auth_code_flow.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentUserServiceTest {

    private final CurrentUserService currentUserService = new CurrentUserService();

    @Test
    void extractsIdentityAndAuthoritiesFromOidcAuthentication() {
        DefaultOAuth2User principal = new DefaultOAuth2User(
                Set.of(
                        new SimpleGrantedAuthority("OIDC_USER"),
                        new SimpleGrantedAuthority("SCOPE_openid")
                ),
                Map.of(
                        "sub", "keycloak-subject",
                        "email", "demo.owner@example.test",
                        "name", "Demo Owner"
                ),
                "sub"
        );
        OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(
                principal,
                principal.getAuthorities(),
                "oauth2-authorization-flow"
        );

        AuthUser authUser = currentUserService.from(token);

        assertThat(authUser.subject()).isEqualTo("keycloak-subject");
        assertThat(authUser.email()).isEqualTo("demo.owner@example.test");
        assertThat(authUser.name()).isEqualTo("Demo Owner");
        assertThat(authUser.roles()).containsExactlyInAnyOrder("OIDC_USER", "SCOPE_openid");
    }
}
