# SaaS Platform Prototype

Dieses Projekt ist ein B2B-SaaS-Prototyp für mandantenfähige Geschäftsprozesse. Der Fokus liegt aktuell auf Authentifizierung, sauberer Backend-Struktur, Tenant-Isolation und einer modernen Angular-Oberfläche.

Es ist ein persönliches Showcase-Projekt und bewusst als kompakter modularer Monolith gehalten. Der nächste fachliche Ausbau soll in Richtung Materials / Inventory Master Data gehen.

## Warum dieses Projekt?

- eigenes Lern- und Showcase-Projekt
- B2B-SaaS-Grundlage mit Mandantenfähigkeit
- Fokus auf Spring Boot Backend, BFF-Auth, PostgreSQL/Flyway und Angular
- technische Regeln wie Tenant-Isolation, CSRF, PKCE und serverseitige Sessions sind bewusst Teil des Designs

## Tech Stack

- Backend: Java 21, Spring Boot, Spring Security OAuth2 Client
- Frontend: Angular
- Datenbank: PostgreSQL
- Migrationen: Flyway
- Auth: Keycloak / OpenID Connect
- Tests: JUnit, Testcontainers, Vitest
- Dev-Setup: Docker Compose für lokale Infrastruktur

## Architektur kurz erklärt

- Spring Boot arbeitet als Backend-for-Frontend.
- Login läuft über Keycloak und OpenID Connect.
- Tokens bleiben serverseitig in der Spring-Session.
- Angular speichert keine JWTs und dekodiert keine Tokens.
- CSRF ist aktiv; PKCE S256 ist aktiv.
- Mandantenfähigkeit basiert auf `Tenant`, `AppUser` und `TenantMembership`.
- Fachliche Daten werden serverseitig tenant-scoped geladen.
- Das Frontend sendet für tenant-owned Daten keine `tenantId`.

## Aktueller Funktionsumfang

- Login über Keycloak
- session-basierte Angular-Anbindung über Cookies
- Benutzer- und Tenant-Kontext
- Projekte anlegen, anzeigen und archivieren
- Tenant-Isolation im Backend
- Flyway-Datenbankschema
- Backend-Tests inklusive PostgreSQL-Testcontainers
- erste moderne Angular-Oberfläche mit Projekttabelle, Dialogen und Sidebar

## Screenshots

Screenshots werden ergänzt.

## Lokal starten

Voraussetzungen:

- Java 21
- Node.js / npm
- Docker Desktop
- lokaler Keycloak-Realm mit passendem OAuth2-Client

PostgreSQL starten:

```powershell
cd backend
docker compose up -d
```

Backend im Dev-Profil starten:

```powershell
cd backend
$env:SPRING_PROFILES_ACTIVE="dev"
$env:KEYCLOAK_CLIENT_SECRET="<placeholder>"
.\mvnw.cmd spring-boot:run
```

Frontend starten:

```powershell
cd frontend
npm start
```

Das Frontend läuft lokal über den Angular Dev Server. API-, Logout- und OAuth2-Aufrufe werden über den Proxy an das Spring Boot Backend weitergeleitet.

## Tests

Backend:

```powershell
cd backend
.\mvnw.cmd verify
```

Frontend:

```powershell
cd frontend
npm run build
npm test -- --watch=false
```

Falls PowerShell `npm.ps1` blockiert:

```powershell
npm.cmd run build
npm.cmd test -- --watch=false
```

## Status

Das Projekt ist ein Prototyp und Work in Progress. Der aktuelle Stand deckt Authentifizierung, Tenant-Kontext, Projekte und grundlegende UI-Flows ab.

Geplanter nächster Schritt: Materials / Inventory Master Data.

## Hinweis

Dieses Repository enthält keine Produktivdaten und ist ein persönliches Showcase-Projekt. Lokale Secrets wie Keycloak-Client-Secrets gehören nicht ins Repository.
