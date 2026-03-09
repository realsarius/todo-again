# Docker Usage (No Local Java/Maven Install)

This project can be built and tested using Docker only.

## Prerequisite
- Docker Desktop (or Docker Engine + Compose plugin)

## Verify toolchain in container

```bash
docker compose run --rm maven -v
```

## Run tests + coverage check

```bash
docker compose run --rm maven clean verify
```

## Run only tests

```bash
docker compose run --rm maven clean test
```

## Build jar/package artifacts

```bash
docker compose run --rm maven clean package -DskipTests
```

## Notes
- Maven dependency cache is persisted in Docker volume: `maven-cache`.
- Source code is mounted from your local working directory into the container.
- JavaFX desktop UI run (`javafx:run`) is not recommended inside container by default, because it requires extra host display forwarding setup.
