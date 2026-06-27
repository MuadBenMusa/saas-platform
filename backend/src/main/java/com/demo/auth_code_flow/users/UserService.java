package com.demo.auth_code_flow.users;

import com.demo.auth_code_flow.security.AuthUser;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final CurrentAppUserService currentAppUserService;

    public UserService(CurrentAppUserService currentAppUserService) {
        this.currentAppUserService = currentAppUserService;
    }

    public CurrentUserResponse getCurrentUser(AuthUser authUser) {
        CurrentAppUser currentUser = currentAppUserService.resolve(authUser);
        return new CurrentUserResponse(
                new CurrentUserResponse.UserSummary(
                        currentUser.userId(),
                        currentUser.email(),
                        currentUser.name(),
                        currentUser.userStatus()
                ),
                new CurrentUserResponse.ActiveTenantSummary(
                        currentUser.tenantId(),
                        currentUser.tenantName(),
                        currentUser.tenantSlug(),
                        currentUser.tenantRole()
                )
        );
    }
}
