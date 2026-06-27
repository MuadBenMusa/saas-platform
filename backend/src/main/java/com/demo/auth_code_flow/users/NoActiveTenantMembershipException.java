package com.demo.auth_code_flow.users;

public class NoActiveTenantMembershipException extends RuntimeException {

    public NoActiveTenantMembershipException() {
        super("No active tenant membership is available for the current user");
    }
}
