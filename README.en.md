# todo-again

A JavaFX desktop to-do application from 2021, revived with a modern Java toolchain.

## Table of Contents

- [0. Quick Setup](#0-quick-setup)
- [1. Project Scope](#1-project-scope)
- [2. Technology Stack](#2-technology-stack)
- [3. Architecture Design](#3-architecture-design)
- [4. Data Format](#4-data-format)
- [5. Logging and Error Handling](#5-logging-and-error-handling)
- [6. Test Strategy](#6-test-strategy)
- [7. Setup and Running](#7-setup-and-running)
- [8. Packaging and Distribution](#9-packaging-and-distribution)
- [9. License](#10-license)

---

## 0. Quick Setup

### Local Setup (JDK + Maven)

```bash
# Run the application
mvn clean javafx:run

# Run tests and coverage check
mvn clean verify
```

### With Docker (no Java/Maven install required)

```bash
# Test + coverage check
docker compose run --rm maven clean verify

# Tests only
docker compose run --rm maven clean test

# Build package (skip tests)
docker compose run --rm maven clean package -DskipTests
```

---

## 1. Project Scope

**Task Management**: Add, edit, delete, and save tasks to file. A simple data model with title, detail, and date fields.

**Persistence**: The task list is written to `Yapilacaklar.txt` on every shutdown using a tab-separated format. This file is read on startup; malformed or missing lines are skipped without crashing the application.

**Pomodoro Timer**: An integrated Pomodoro model for focus session tracking. A sound effect plays when the session ends.

**Sound Effects**: Custom audio files for button clicks and dialog actions (`cancelButon.mp3`, `dialog.mp3`, `dialogOkButon.mp3`).

**Revival Goal**: The project was revived to migrate a 2021 codebase to a modern Java toolchain (Java 21, Maven, JUnit 5, JaCoCo) and establish a solid CI infrastructure. The JFoenix dependency was removed; standard JavaFX controls are used throughout.

---

## 2. Technology Stack

| Category | Technology / Library | Purpose |
|---|---|---|
| **Runtime** | Java 21 (LTS) | Long-term support, modern language features |
| **UI Framework** | JavaFX 21 (`controls`, `fxml`, `media`) | Desktop UI, FXML-based layout |
| **Build** | Maven 3.9+ | Dependency management, packaging, plugin ecosystem |
| **Serialization** | Gson 2.11 | JSON-based data backup |
| **Utility** | Apache Commons Lang 3 | String and general utility methods |
| **Testing** | JUnit 5 (Jupiter) | Unit tests |
| **Coverage** | JaCoCo | Branch coverage report and threshold enforcement |
| **DevOps** | Docker, Docker Compose | Environment-independent build and test |
| **CI** | GitHub Actions | Automated build + test pipeline |
| **Packaging** | jpackage (JDK 21) | macOS DMG generation |

---

## 3. Architecture Design

`todo-again` is a single-tier JavaFX desktop application with file-based persistence.

Detailed architecture document: [`docs/architecture.md`](docs/architecture.md)

### Startup Flow

1. `Main` starts JavaFX and loads `test.fxml`.
2. `Controller` wires UI events to domain and persistence operations.
3. `YapilacakVeri` singleton manages the in-memory list and disk I/O.
4. On application shutdown, the current list is written to `Yapilacaklar.txt`.

### Package Structure

```
src/yapilacaklarListesi/
├── Main.java                      # JavaFX entry point
├── Controller.java                # Main screen UI controller
├── DialogController.java          # Task add/edit dialog
├── test.fxml                      # Main screen layout
├── yapilacakDialogEkrani.fxml     # Dialog layout
├── veriler/
│   ├── Yapilacak.java             # Domain model (title, detail, date)
│   └── YapilacakVeri.java         # Persistence service (read/write)
├── pomodoro/model/                # Pomodoro timer model
├── mediator/                      # Pomodoro string render adapter
└── muzik/                         # Sound player abstractions
```

### Quality Gates

- **Branch Coverage**: JaCoCo threshold for `YapilacakVeri` >= 70%
- **CI**: `mvn -B verify` runs automatically on every push and PR

---

## 4. Data Format

Tasks are stored in `Yapilacaklar.txt` as tab-separated lines.

### Record Format

```
description<TAB>detail<TAB>dd-MM-yyyy
```

### Example

```
Go shopping	Milk, bread, yogurt	24-03-2026
Finish report	Q1 sales report draft	25-03-2026
```

### Defensive Loading Behavior

- The file is auto-created if missing.
- Lines with incorrect tab counts are skipped with a warning log.
- Lines with an invalid date format are silently skipped; the application does not crash.

### JSON Backup

Additionally, `Yapilacaklar.json` can be maintained as a Gson-generated JSON backup.

---

## 5. Logging and Error Handling

### Console Logging

`YapilacakVeri` uses standard Java `System.err` / `System.out` based logging for read/write errors. Malformed data lines are printed as a warning before being skipped.

### Error Dialogs

The UI layer shows meaningful `Alert` dialogs to the user:

- Attempting to add a task with an empty title or invalid date
- Attempting to delete or edit without a selection
- File save errors

### NPE Protection

Critical null-reference risks identified during the revival were fixed:

- `FileChooser` result `null` check
- Delete/edit guard when no task is selected
- Date field made non-skippable

---

## 6. Test Strategy

### 6.1 Unit Tests

Unit tests for the `YapilacakVeri` persistence service are written with JUnit 5 (Jupiter).

Covered scenarios:

- Save and reload tasks
- Malformed data line (skip behavior)
- Empty file initialization
- Legacy format (tab-separated) compatibility

### 6.2 Coverage Target

JaCoCo reports **branch coverage >= 70%** for the `YapilacakVeri` class as part of CI.

### 6.3 Test Commands

```bash
# Run all tests and coverage check
mvn clean verify

# Run tests only
mvn clean test

# Via Docker (no Java/Maven install required)
docker compose run --rm maven clean verify
```

---

## 7. Setup and Running

### 7.1 Prerequisites

**For local setup:**

- JDK 21 (accessible in PATH)
- Maven 3.9+

```bash
java -version
mvn -version
```

**For Docker setup:**

- Docker Desktop or Docker Engine + Compose plugin

### 7.2 Run the Application

```bash
# Local
mvn clean javafx:run

# Docker (GUI not supported, build/test only)
docker compose run --rm maven clean verify
```

> **Note:** The Docker service is configured with `platform: linux/amd64` for consistent JavaFX artifact resolution on ARM hosts. Running the desktop UI (`javafx:run`) inside Docker requires additional display forwarding setup.

### 7.3 Data File

The application uses `Yapilacaklar.txt` in the working directory by default.

- The file is auto-created on startup if missing.
- Format: `description<TAB>detail<TAB>dd-MM-yyyy`

### 7.4 DMG Package Generation

DMG generation requires a macOS runner. GitHub Actions is the most practical approach.

**Via GitHub Actions:**

1. Go to the `Actions` tab.
2. Select the `Build DMG` workflow.
3. Click `Run workflow`.
4. Download the `todo-again-dmg` artifact when the job finishes.

**On local macOS:**

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

mvn -DskipTests clean package dependency:copy-dependencies -DincludeScope=runtime

JAR_PATH=$(ls target/todo-again-*.jar | head -n 1)
JAR_FILE=$(basename "$JAR_PATH")
mkdir -p target/jpackage-input
cp "$JAR_PATH" target/jpackage-input/
cp target/dependency/*.jar target/jpackage-input/

jpackage \
  --type dmg \
  --name "todo-again" \
  --dest target/dist \
  --input target/jpackage-input \
  --main-jar "$JAR_FILE" \
  --main-class yapilacaklarListesi.Main \
  --app-version "1.0.1"
```

Detailed guide: [`docs/local-test.md`](docs/local-test.md)

---

## 8. Screenshots

![screenshot-1](ss1.png)
![screenshot-2](ss2.png)
![screenshot-3](ss3.png)

---

## 9. Packaging and Distribution

### 9.1 CI Pipeline

`.github/workflows/ci.yml` — Triggered on every push and PR:

- `mvn -B verify` (compile + test + JaCoCo coverage check)
- Runs on Java 21

### 9.2 DMG Build Pipeline

`.github/workflows/dmg.yml` — Produces a DMG package on a macOS runner:

- **Manual trigger**: `Actions` → `Build DMG` → `Run workflow` (tag input required)
- **Tag trigger**: Pushing a tag like `v1.0.1`
- **Output artifact**: `todo-again-dmg`

### 9.3 Docker Notes

The Maven dependency cache is stored in the `maven-cache` Docker volume. Source code is mounted from the local working directory into the container.

### 9.4 Basic Verification

To verify build and test success:

```bash
# Local
mvn clean verify

# Docker
docker compose run --rm maven clean verify
```

---

## 10. License

MIT — See the [`LICENSE`](LICENSE) file in the root directory for details.