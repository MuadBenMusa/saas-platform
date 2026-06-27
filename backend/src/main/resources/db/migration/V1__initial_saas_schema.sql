CREATE TABLE tenants (
                         id UUID PRIMARY KEY,
                         name VARCHAR(120) NOT NULL,
                         slug VARCHAR(80) NOT NULL,
                         status VARCHAR(30) NOT NULL,
                         created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                         CONSTRAINT uk_tenants_name UNIQUE (name),
                         CONSTRAINT uk_tenants_slug UNIQUE (slug)
);

CREATE TABLE app_users (
                           id UUID PRIMARY KEY,
                           keycloak_subject VARCHAR(80) NOT NULL,
                           email VARCHAR(180) NOT NULL,
                           name VARCHAR(180) NOT NULL,
                           status VARCHAR(30) NOT NULL,
                           created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                           CONSTRAINT uk_app_users_keycloak_subject UNIQUE (keycloak_subject)
);

CREATE TABLE tenant_memberships (
                                    id UUID PRIMARY KEY,
                                    user_id UUID NOT NULL,
                                    tenant_id UUID NOT NULL,
                                    role VARCHAR(30) NOT NULL,
                                    status VARCHAR(30) NOT NULL,
                                    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                    CONSTRAINT fk_tenant_memberships_user
                                        FOREIGN KEY (user_id)
                                            REFERENCES app_users (id),

                                    CONSTRAINT fk_tenant_memberships_tenant
                                        FOREIGN KEY (tenant_id)
                                            REFERENCES tenants (id),

                                    CONSTRAINT uk_tenant_memberships_user_tenant
                                        UNIQUE (user_id, tenant_id)
);

CREATE TABLE projects (
                          id UUID PRIMARY KEY,
                          tenant_id UUID NOT NULL,
                          project_number VARCHAR(60) NOT NULL,
                          name VARCHAR(200) NOT NULL,
                          description VARCHAR(2000),
                          status VARCHAR(30) NOT NULL,
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                          updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                          CONSTRAINT fk_projects_tenant
                              FOREIGN KEY (tenant_id)
                                  REFERENCES tenants (id),

                          CONSTRAINT uk_projects_tenant_project_number
                              UNIQUE (tenant_id, project_number)
);

CREATE TABLE audit_logs (
                            id UUID PRIMARY KEY,
                            actor_subject VARCHAR(80) NOT NULL,
                            action VARCHAR(120) NOT NULL,
                            entity_type VARCHAR(80) NOT NULL,
                            entity_id VARCHAR(80),
                            created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_app_users_keycloak_subject
    ON app_users (keycloak_subject);

CREATE INDEX idx_app_users_email
    ON app_users (email);

CREATE INDEX idx_tenant_memberships_user
    ON tenant_memberships (user_id);

CREATE INDEX idx_tenant_memberships_tenant
    ON tenant_memberships (tenant_id);

CREATE INDEX idx_projects_tenant_id
    ON projects (tenant_id);

CREATE INDEX idx_projects_tenant_status
    ON projects (tenant_id, status);
