# Patois: Modular Type-Safe i18n for Kotlin

## Overview
**Patois** is a modular Kotlin Multiplatform (KMP) library for type-safe internationalization. It separates the core runtime, code generation, and framework integrations to ensure maximum flexibility and performance.

**Core Philosophy:**
*   **Agnostic Core:** The generated code is pure Kotlin (Objects/Interfaces) with zero dependencies on UI or Server frameworks.
*   **Extensions:** Framework-specific behavior (Compose reactivity, Ktor request scoping) is handled by separate extension modules.
*   **Build-Time Safety:** Errors (missing keys, typos) are caught at compile time.

---

## Architecture

The project is structured as a multi-module Gradle project:

### 1. `patois-core` (KMP)
*   **Role**: The lightweight runtime foundation.
*   **Targets**: `common`, `jvm`, `js`, `native`, `android`.
*   **Dependencies**: None (Standard Library only).
*   **Content**:
    *   **Interfaces**: `TranslationProvider`, `PluralResolver`.
    *   **Logic**: Rules for handling plurals ("one", "few", "many").
    *   **Annotations**: `@TranslationFile` (if needed to mark config classes).

### 2. `patois-generator` (Gradle Plugin - JVM)
*   **Role**: Gradle plugin for code generation.
*   **Inputs**: JSON/YAML files in `src/commonMain/resources/i18n`.
*   **Outputs**: Pure Kotlin code (Interfaces and Object implementations).
*   **Behavior**:
    *   Scans for translation files matching a pattern (e.g., `*.i18n.json`).
    *   Reads the base language file (e.g., `en.i18n.json`) to generate the **Contract Interface**.
    *   Reads other files (e.g., `de.i18n.json`) to generate **Implementation Objects**.
    *   It does **NOT** generate Compose/Ktor specific code.

### 3. `patois-extension-compose` (KMP)
*   **Role**: Bindings for Jetpack Compose / Compose Multiplatform.
*   **Targets**: `android`, `ios`, `desktop`, `wasm`.
*   **Dependencies**: `patois-core`, `androidx.compose.runtime`.
*   **Features**:
    *   `val LocalTranslations = staticCompositionLocalOf { ... }`
    *   `PatoisTheme { ... }` wrapper to provide translations.
    *   Reactive updates: Changing the locale triggers a recomposition.

### 4. `patois-extension-ktor` (JVM)
*   **Role**: Server-side integration.
*   **Targets**: `jvm`.
*   **Dependencies**: `patois-core`, `io.ktor:ktor-server-core`.
*   **Features**:
    *   **Ktor Plugin**: `install(Patois) { ... }`
    *   **Resolution Strategy**: Cookie -> Header -> Default.
    *   **Call Extension**: `call.i18n` returns the correct Translation object for *that specific request*.

---

## Workflow

1.  **Setup**: User applies the `patois` Gradle plugin and adds `patois-core`.
2.  **Define**: User creates `commonMain/resources/i18n/en.i18n.json`.
3.  **Generate**: Build process runs `generateTranslations` task. `patois-generator` creates `object ENTranslations : AppTranslations`.
4.  **Usage (Shared Logic)**:
    ```kotlin
    fun getWelcomeMessage(t: AppTranslations, name: String): String {
        return t.welcome(name)
    }
    ```
5.  **Usage (Android/Compose)**:
    ```kotlin
    // In Activity/Main
    PatoisHost(translations = EnTranslations) {
        // UI code
        Text(text = Patois.current.welcome("Alice"))
    }
    ```
6.  **Usage (Ktor)**:
    ```kotlin
    get("/") {
        call.respond(call.i18n.welcome("User"))
    }
    ```

---

## Development Roadmap

### Phase 1: Foundation (The Core & Processor)
*   [ ] **Scaffold**: Set up the multi-module Gradle project structure.
*   [ ] **Core**: Implement `PluralResolver` in `patois-core`.
*   [ ] **Processor**: Create `patois-generator` to read a flat JSON and generate a Kotlin Object.
    *   *Goal*: `println(GeneratedTranslations.en.hello)` works.

### Phase 2: Refinement
*   [ ] **Nested Keys**: Support `home.title` -> `home { val title: String }`.
*   [ ] **Parameters**: Support `hello: "Hi {name}"` -> `fun hello(name: String)`.
*   [ ] **Interfaces**: Generate a common interface `AppTranslations` so implementations can be swapped.

### Phase 3: Extensions
*   [ ] **Compose**: Create `patois-extension-compose` with `CompositionLocal`.
*   [ ] **Ktor**: Create `patois-extension-ktor` with a Plugin.

### Phase 4: Polish
*   [ ] **Formats**: Add YAML and CSV support.
*   [ ] **Validation**: Fail build if keys are missing in secondary languages.
