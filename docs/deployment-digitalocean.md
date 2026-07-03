# DigitalOcean App Platform deployment notes

These notes document the public-showcase deployment shape. They intentionally use placeholders only; do not commit real secrets, database URLs with passwords, admin passwords, or client secrets.

## Architecture

- Keycloak runs as a separate DigitalOcean App Platform container app.
- The Spring Boot backend and Angular static frontend run under one App Platform app/domain.
- Angular uses same-origin relative URLs such as `/api/projects`.
- OAuth2 tokens stay server-side in the Spring session. The browser uses the Spring session cookie plus CSRF protection.

## Keycloak app environment variables

Set these in the separate Keycloak App Platform app. Use real values only in DigitalOcean, not in the repository.

```text
KC_DB=postgres
KC_DB_URL=jdbc:postgresql://<keycloak-db-host>:<port>/<database>
KC_DB_USERNAME=<keycloak-db-user>
KC_DB_PASSWORD=<keycloak-db-password>
KC_HOSTNAME=<keycloak-public-hostname-or-url>
KC_PROXY_HEADERS=xforwarded
KC_HEALTH_ENABLED=true
KC_METRICS_ENABLED=true
KC_BOOTSTRAP_ADMIN_USERNAME=<admin-username>
KC_BOOTSTRAP_ADMIN_PASSWORD=<admin-password>
```

The `keycloak/Dockerfile` currently builds `quay.io/keycloak/keycloak:26.6.4` with health and metrics enabled before `kc.sh build`.

## Backend app environment variables

Set these on the Spring Boot backend service.

```text
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://<backend-db-host>:<port>/<database>
SPRING_DATASOURCE_USERNAME=<backend-db-user>
SPRING_DATASOURCE_PASSWORD=<backend-db-password>
KEYCLOAK_CLIENT_ID=oauth2-authorization-flow
KEYCLOAK_CLIENT_SECRET=<keycloak-confidential-client-secret>
KEYCLOAK_ISSUER_URI=https://<keycloak-domain>/realms/<realm>
APP_LOGIN_SUCCESS_REDIRECT_URI=https://<app-domain>/dashboard
APP_POST_LOGOUT_REDIRECT_URI=https://<app-domain>/login?logout
```

The OAuth2 registration id must remain `oauth2-authorization-flow` because the login start path is:

```text
/oauth2/authorization/oauth2-authorization-flow
```

## Frontend static site settings

```text
Source dir: frontend
Build command: npm ci && npm run build
Output dir: dist/saas-frontend/browser
SPA catch-all: index.html
```

The SPA catch-all is required so browser refreshes on Angular routes such as `/dashboard`, `/projects`, and `/login` return `index.html`.

## Backend service settings

```text
Health path: /actuator/health
HTTP port: 8080
```

The backend service must preserve path prefixes for all Spring-owned routes. Without this, App Platform can strip the route prefix before the request reaches Spring Security, breaking API calls and OAuth2 login/logout callbacks.

Required backend routes:

```text
/api
/oauth2
/login/oauth2
/logout
/actuator
```

Each backend route must set:

```text
preserve_path_prefix: true
```

## Example App Spec snippet

This is a shape example only. Keep real values and secrets in DigitalOcean environment configuration.

```yaml
name: saas-platform

services:
  - name: backend
    source_dir: backend
    dockerfile_path: backend/Dockerfile
    http_port: 8080
    health_check:
      http_path: /actuator/health
    envs:
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      - key: SPRING_DATASOURCE_URL
        value: jdbc:postgresql://<backend-db-host>:<port>/<database>
        type: SECRET
      - key: SPRING_DATASOURCE_USERNAME
        value: <backend-db-user>
        type: SECRET
      - key: SPRING_DATASOURCE_PASSWORD
        value: <backend-db-password>
        type: SECRET
      - key: KEYCLOAK_CLIENT_ID
        value: oauth2-authorization-flow
      - key: KEYCLOAK_CLIENT_SECRET
        value: <keycloak-confidential-client-secret>
        type: SECRET
      - key: KEYCLOAK_ISSUER_URI
        value: https://<keycloak-domain>/realms/<realm>
      - key: APP_LOGIN_SUCCESS_REDIRECT_URI
        value: https://<app-domain>/dashboard
      - key: APP_POST_LOGOUT_REDIRECT_URI
        value: https://<app-domain>/login?logout
    routes:
      - path: /api
        preserve_path_prefix: true
      - path: /oauth2
        preserve_path_prefix: true
      - path: /login/oauth2
        preserve_path_prefix: true
      - path: /logout
        preserve_path_prefix: true
      - path: /actuator
        preserve_path_prefix: true

static_sites:
  - name: frontend
    source_dir: frontend
    build_command: npm ci && npm run build
    output_dir: dist/saas-frontend/browser
    index_document: index.html
    catchall_document: index.html
    routes:
      - path: /
```

## Manual setup still required

This showcase repo does not currently include automated production provisioning for Keycloak realms, clients, users, tenants, or memberships. A production-like deployment still requires:

- manual Keycloak realm and confidential client setup
- Authorization Code Flow with PKCE S256
- valid redirect URI for `/login/oauth2/code/oauth2-authorization-flow`
- valid post-logout redirect URI
- manual demo user linkage to `AppUser`, `Tenant`, and `TenantMembership`
