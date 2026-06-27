# SaaS Platform Keycloak Theme

This directory contains a custom, CSS-only Keycloak theme designed to match the SaaS Angular frontend. It extends the built-in `keycloak` theme, prioritizing safety and maintainability over complex template overrides.

## Contents
- `theme.properties`: Defines the parent theme and loads our custom CSS.
- `saas-login.css`: Contains CSS rules overriding Keycloak's default PatternFly styling.
- `logo.svg`: A simple abstract SVG logo used on the login form.

## How to use locally

If you are running Keycloak natively or via an external Docker script, you need to make this theme available to Keycloak.

### Via Docker-Compose Volume
If Keycloak is later added to the `backend/docker-compose.yml`, mount this directory as follows:

```yaml
services:
  keycloak:
    image: quay.io/keycloak/keycloak:latest
    ...
    volumes:
      - ../keycloak/themes/saas-platform:/opt/keycloak/themes/saas-platform
```

### Standalone Keycloak
Copy the `saas-platform` directory into your Keycloak installation's `themes/` directory:
```bash
cp -r keycloak/themes/saas-platform /path/to/keycloak/themes/
```

## Activation Steps
Once the theme files are accessible to Keycloak:

1. Log in to the Keycloak Admin Console.
2. Ensure you have selected the correct realm (e.g., `oauth2-demos`).
3. In the left navigation, go to **Realm Settings**.
4. Click on the **Themes** tab.
5. In the **Login Theme** dropdown, select `saas-platform`.
6. Click **Save**.

## Local Development & Caching
Keycloak caches themes aggressively to improve performance. For local theme development, you should start Keycloak with caching disabled.

If using Keycloak 17+ (Quarkus), the `start-dev` command automatically disables caching. 
If you need to manually disable it, start Keycloak with:
```bash
kc.sh start-dev --spi-theme-static-max-age=-1 --spi-theme-cache-themes=false --spi-theme-cache-templates=false
```

## Verification Checklist
When testing the theme, verify:
- [ ] Login page has a light blue gradient background and a rounded white card.
- [ ] The primary 'Sign In' button is dark navy (`#0f3b73`).
- [ ] Invalid credentials display a clearly legible warning/error banner.
- [ ] The 'Forgot Password' link is visible and readable.
- [ ] The layout is responsive on mobile screens.
- [ ] The login flow still correctly redirects to `http://localhost:4200/dashboard` upon success.
- [ ] Logout via the application correctly redirects to the Keycloak logout and back to `http://localhost:4200/login?logout`.
