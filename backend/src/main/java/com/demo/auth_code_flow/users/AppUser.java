package com.demo.auth_code_flow.users;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "app_users",
        indexes = {
                @Index(name = "idx_app_users_keycloak_subject", columnList = "keycloak_subject"),
                @Index(name = "idx_app_users_email", columnList = "email")
        }
)
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "keycloak_subject", nullable = false, unique = true, length = 80)
    private String keycloakSubject;

    @Column(name = "email", nullable = false, length = 180)
    private String email;

    @Column(name = "name", nullable = false, length = 180)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected AppUser() {
    }

    public AppUser(String keycloakSubject, String email, String name) {
        this.keycloakSubject = keycloakSubject;
        this.email = email;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getKeycloakSubject() {
        return keycloakSubject;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void disable() {
        this.status = UserStatus.DISABLED;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public enum UserStatus {
        ACTIVE,
        DISABLED
    }
}
