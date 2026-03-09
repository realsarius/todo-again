# Architecture Overview

## Runtime model
`todo-again` is a JavaFX desktop application with file-based persistence.

Main flow:
1. `Main` starts JavaFX and loads `test.fxml`.
2. `Controller` wires UI events to domain and persistence operations.
3. `YapilacakVeri` singleton manages in-memory list and disk I/O.
4. On app shutdown, the current list is persisted to `Yapilacaklar.txt`.

## Packages
- `yapilacaklarListesi`
  - App bootstrap (`Main`)
  - UI controllers (`Controller`, `DialogController`)
- `yapilacaklarListesi.veriler`
  - Domain model (`Yapilacak`)
  - Persistence service (`YapilacakVeri`)
- `yapilacaklarListesi.pomodoro.model`
  - Pomodoro timer model
- `yapilacaklarListesi.mediator`
  - Small display adapter for pomodoro string rendering
- `yapilacaklarListesi.muzik`
  - Sound player abstractions and implementations

## UI
- FXML files:
  - `src/yapilacaklarListesi/test.fxml`
  - `src/yapilacaklarListesi/yapilacakDialogEkrani.fxml`
- Controls are now standard JavaFX controls (`ListView`, `ToggleButton`, etc.).

## Persistence
- Storage file: `Yapilacaklar.txt`
- Record format: `aciklama<TAB>detay<TAB>dd-MM-yyyy`
- Defensive behavior:
  - Missing file is auto-created.
  - Malformed or date-invalid lines are skipped with warning logs.

## Build and module config
- Build tool: Maven (`pom.xml`)
- Java module descriptor: `src/module-info.java`
- JavaFX launch: `javafx-maven-plugin`

## Quality gates
- Unit tests: JUnit 5
- Coverage: JaCoCo (`YapilacakVeri` branch coverage target >= 70%)
- CI: GitHub Actions (`.github/workflows/ci.yml`)
