# learning-demo

A multi-module Maven workspace of small, self-contained Java use-case demos.
Each module is an independent Spring Boot application with its own `pom.xml`,
README, and test suite, built with a stable Spring Boot release (3.3.x) and
Java 21.

## Modules

| Module | Use case |
|---|---|
| [`oauth2-client-credentials-jwt`](oauth2-client-credentials-jwt/README.md) | OAuth2 Client Credentials grant: issuing and validating signed JWT access tokens, scope-based authorization |

## Building

```bash
mvn -q -T1C test        # build and test every module
mvn -q -pl <module> test # build and test a single module
```

## Adding a new use case

1. Create a new directory at the repo root, e.g. `mvn archetype:generate` or copy
   the structure of an existing module.
2. Give the module's `pom.xml` a `<parent>` of `spring-boot-starter-parent`
   (pick the same stable version other modules use) so it builds and runs as
   its own Spring Boot application.
3. Register the new directory under `<modules>` in the root `pom.xml`.
4. Add a module-level `README.md` describing the use case, how to run it, and
   how to exercise it (curl examples, tests).
