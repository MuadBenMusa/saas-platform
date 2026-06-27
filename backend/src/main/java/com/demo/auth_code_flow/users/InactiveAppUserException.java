package com.demo.auth_code_flow.users;

public class InactiveAppUserException extends RuntimeException {

    public InactiveAppUserException() {
        super("SaaS user is not active");
    }
}
