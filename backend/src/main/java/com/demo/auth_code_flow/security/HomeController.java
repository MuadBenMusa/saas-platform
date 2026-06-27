package com.demo.auth_code_flow.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public HomeResponse home() {
        return new HomeResponse(
                "auth-code-flow",
                "UP",
                "/oauth2/authorization/oauth2-authorization-flow",
                "POST /logout, or open /logout and use Spring's confirmation page"
        );
    }

    public record HomeResponse(
            String application,
            String status,
            String loginUrl,
            String logoutInfo
    ) {
    }
}
