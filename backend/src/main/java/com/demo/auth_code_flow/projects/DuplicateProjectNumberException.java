package com.demo.auth_code_flow.projects;

public class DuplicateProjectNumberException extends RuntimeException {

    public DuplicateProjectNumberException(String projectNumber) {
        super("Project number already exists for the active tenant: " + projectNumber);
    }
}
