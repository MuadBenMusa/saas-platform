package com.demo.auth_code_flow.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;

import static org.assertj.core.api.Assertions.assertThat;

class PkceAuthorizationRequestResolverTest {

    @Test
    void addsS256PkceToConfidentialClientAuthorizationRequest() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("keycloak")
                .clientId("bff-client")
                .clientSecret("test-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid")
                .authorizationUri("https://identity.example.test/authorize")
                .tokenUri("https://identity.example.test/token")
                .jwkSetUri("https://identity.example.test/jwks")
                .userInfoUri("https://identity.example.test/userinfo")
                .userNameAttributeName("sub")
                .clientName("Keycloak")
                .clientSettings(ClientRegistration.ClientSettings.builder().requireProofKey(false).build())
                .build();
        InMemoryClientRegistrationRepository registrations =
                new InMemoryClientRegistrationRepository(registration);
        OAuth2AuthorizationRequestResolver resolver =
                new SecurityConfig().pkceAuthorizationRequestResolver(registrations);

        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/oauth2/authorization/keycloak");
        request.setServletPath("/oauth2/authorization/keycloak");
        request.setScheme("https");
        request.setServerName("app.example.test");
        request.setServerPort(443);

        OAuth2AuthorizationRequest authorizationRequest = resolver.resolve(request);

        assertThat(authorizationRequest).isNotNull();
        assertThat(authorizationRequest.getAdditionalParameters())
                .containsKey(PkceParameterNames.CODE_CHALLENGE)
                .containsEntry(PkceParameterNames.CODE_CHALLENGE_METHOD, "S256");
        Object codeVerifier = authorizationRequest.getAttribute(PkceParameterNames.CODE_VERIFIER);
        assertThat(codeVerifier)
                .isInstanceOf(String.class);
    }
}
