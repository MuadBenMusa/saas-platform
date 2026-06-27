package com.demo.auth_code_flow.users;

public class CurrentUserNotProvisionedException extends RuntimeException {

    public CurrentUserNotProvisionedException() {
        super("Authenticated user is not provisioned in the SaaS application");
    }
}
