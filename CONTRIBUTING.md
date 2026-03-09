# Contributing

Thanks for contributing to `todo-again`.

## Setup
1. Install JDK 21 and Maven 3.9+.
2. Clone the repository.
3. Run:

```bash
mvn clean verify
```

## Development workflow
1. Create a branch from `main`.
2. Make focused changes.
3. Run local checks:

```bash
mvn clean verify
```

4. Open a pull request.

## Commit style
Use clear, scoped commit messages. Example:

```text
feat(data): improve malformed line handling

- skip malformed rows during parsing
- add migration tests for legacy file format
```

## Pull request checklist
- App builds on Java 21.
- Tests pass locally.
- Coverage check passes.
- Docs updated when behavior changes.

## Notes
- Keep legacy data compatibility in mind (`Yapilacaklar.txt`).
- Prefer defensive parsing and explicit error handling.
