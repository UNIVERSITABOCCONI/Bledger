# Backend Internal README

## Overview
This module is the reactive Spring Boot backend for the Bocconi MUSA project. It exposes WebFlux APIs, persists data in PostgreSQL via R2DBC, runs schema migrations with Liquibase, and integrates with blockchain services via web3j.

## Tech Stack
- Java 21 + Gradle
- Spring Boot (WebFlux, R2DBC)
- PostgreSQL + Liquibase
- web3j (blockchain integration)

## Requirements
- Java 21
- Gradle (via `./gradlew`)
- PostgreSQL (local or remote)

## Project Layout (backend/)
- `src/main/java`: application code
- `src/main/resources`: configuration, Liquibase changelogs, default data
- `src/main/solidity`: smart contract sources and Hardhat config
- `postman/`: API collection

## Run Locally
1) Start a postegres instance and run following init script (replace variables)

    ```sql
    -- Create application user (for runtime operations)
    CREATE ROLE ${postgres_app_user} LOGIN PASSWORD '${postgres_app_password}';
    -- Grant basic connection privileges
    GRANT CONNECT ON DATABASE bledger TO ${postgres_app_user};

    -- Create migration user (for Liquibase DDL operations)
    CREATE ROLE ${postgres_migration_user} LOGIN PASSWORD '${postgres_migration_password}';
    GRANT CONNECT ON DATABASE bledger TO ${postgres_migration_user};
    -- Grant DDL privileges for migrations
    GRANT CREATE ON DATABASE bledger TO ${postgres_migration_user};

    -- Create dedicated schema
    CREATE SCHEMA IF NOT EXISTS bledger_schema AUTHORIZATION postgres;

    -- Grant schema usage to app user
    GRANT USAGE ON SCHEMA bledger_schema TO ${postgres_app_user};
    GRANT USAGE ON SCHEMA bledger_schema TO ${postgres_migration_user};

    -- Grant table privileges to app user (for existing and future tables)
    GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA bledger_schema TO ${postgres_app_user};
    GRANT USAGE ON ALL SEQUENCES IN SCHEMA bledger_schema TO ${postgres_app_user};

    -- Set default privileges for future objects created by migration user
    ALTER DEFAULT PRIVILEGES FOR USER ${postgres_migration_user} IN SCHEMA bledger_schema GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO ${postgres_app_user};
    ALTER DEFAULT PRIVILEGES FOR USER ${postgres_migration_user} IN SCHEMA bledger_schema GRANT USAGE ON SEQUENCES TO ${postgres_app_user};

    -- Grant full privileges to migration user on schema
    GRANT ALL PRIVILEGES ON SCHEMA bledger_schema TO ${postgres_migration_user};
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA bledger_schema TO ${postgres_migration_user};
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA bledger_schema TO ${postgres_migration_user};

    -- Set default privileges for migration user
    ALTER DEFAULT PRIVILEGES FOR USER ${postgres_migration_user} IN SCHEMA bledger_schema GRANT ALL PRIVILEGES ON TABLES TO bledger_migration_user;
    ALTER DEFAULT PRIVILEGES FOR USER ${postgres_migration_user} IN SCHEMA bledger_schema GRANT ALL PRIVILEGES ON SEQUENCES TO bledger_migration_user;

    -- Grant privileges on public schema for existing tables (workaround for explicit public. references in migration scripts)
    GRANT USAGE ON SCHEMA public TO ${postgres_migration_user};
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ${postgres_migration_user};
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ${postgres_migration_user};
    ALTER DEFAULT PRIVILEGES FOR USER ${postgres_migration_user} IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO ${postgres_migration_user};
    ALTER DEFAULT PRIVILEGES FOR USER ${postgres_migration_user} IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO ${postgres_migration_user};

    ```
2) Configure environment variables (see below).
3) Run the app:

```bash
./gradlew bootRun
```

The server listens on port `8081` by default.

## Configuration
App defaults are defined in `backend/src/main/resources/application.properties`. Override via environment variables:

- `DB_URL` (default: `r2dbc:postgresql://localhost:5432/bledger?schema=bledger_schema`)
- `DB_APP_USERNAME` (default: `bledger_app_user`)
- `DB_APP_PASSWORD` (default: `bledger_app_user`)
- `DB_LIQUIBASE_URL` (default: `jdbc:postgresql://localhost:5432/bledger`)
- `DB_MIGRATION_USERNAME` (default: `bledger_migration_user`)
- `DB_MIGRATION_PASSWORD` (default: `bledger_migration_user`)
- `ADMIN_WALLET_PRIVATE_KEY` (required for blockchain admin operations)
- `AM_USERNAME`, `AM_PASSWORD` (AM credentials)
- `METADATA_API_URL` (optional override for metadata base URL)

## Database Migrations
Liquibase runs automatically at startup using:

- `backend/src/main/resources/db.changelog/app-changelog-master.xml`
- SQL scripts in `backend/src/main/resources/db.changelog/scripts/`

## Tests
```bash
./gradlew test
```

## Solidity / Smart Contracts
The Solidity tooling lives in `backend/src/main/solidity`. If you need contract tests or compilation:

```bash
cd src/main/solidity
npm install
npx hardhat test
```

## Troubleshooting
- DB connection issues: verify `DB_URL`, credentials, and that `bledger_schema` exists.
- Liquibase errors: confirm migration user permissions and `DB_LIQUIBASE_URL`.
- Blockchain errors: verify `ADMIN_WALLET_PRIVATE_KEY` and network config in `application.properties`.
