package com.demo.auth_code_flow.users;

import com.demo.auth_code_flow.security.AuthUser;
import com.demo.auth_code_flow.security.CurrentUserService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final CurrentUserService currentUserService;
    private final UserService userService;

    public UserController(CurrentUserService currentUserService, UserService userService) {
        this.currentUserService = currentUserService;
        this.userService = userService;
    }

    @GetMapping("/api/users/me")
    public CurrentUserResponse currentAppUser(OAuth2AuthenticationToken token) {
        AuthUser authUser = currentUserService.from(token);
        return userService.getCurrentUser(authUser);
    }
}
