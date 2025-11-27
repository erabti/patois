# Patois Generator - Gradle Plugin

This module contains a Gradle plugin that generates type-safe Kotlin translations from JSON/YAML files.

## Architecture

The plugin is structured as follows:

```
patois-generator/
├── src/main/kotlin/io/github/erabti/patois/plugin/
│   ├── PatoisPlugin.kt              # Main plugin entry point
│   ├── PatoisExtension.kt           # DSL configuration
│   ├── GenerateTranslationsTask.kt  # Gradle task for code generation
│   ├── parser/
│   │   ├── TranslationParser.kt     # Parser interface
│   │   ├── JsonParser.kt            # JSON parser implementation
│   │   └── YamlParser.kt            # YAML parser implementation
│   ├── model/
│   │   └── TranslationNode.kt       # AST models for translation structure
│   └── generator/
│       └── KotlinGenerator.kt       # KotlinPoet-based code generator
```

## How It Works

### 1. File Scanning
The plugin scans for translation files matching a configurable pattern (default: `*.i18n.(json|yaml|yml)`).

### 2. Parsing
Files are parsed into an AST structure (`TranslationNode`) which supports:
- **Leaf nodes**: Simple key-value translations with optional parameters
- **Container nodes**: Nested translation structures

### 3. Code Generation
Using KotlinPoet, the plugin generates:
- **Interface**: `AppTranslations` from the base locale file (defines the contract)
- **Objects**: Locale-specific implementations (e.g., `ENTranslations`, `DETranslations`)

### 4. Output
Generated code is written to `build/generated/patois` (configurable) and automatically added to the Kotlin source set.

## Configuration

Users can configure the plugin in their `build.gradle.kts`:

```kotlin
plugins {
    id("io.github.erabti.patois") version "1.0.0"
}

patois {
    inputDir.set(project.file("src/commonMain/resources/i18n"))
    outputDir.set(project.file("build/generated/patois"))
    baseLocale.set("en")
    filePattern.set(".*\\.i18n\\.(json|yaml|yml)")
    packageName.set("com.example.i18n")
}
```

## Example

Given `en.i18n.json`:
```json
{
  "welcome": "Welcome {name}!",
  "home": {
    "title": "Home"
  }
}
```

The plugin generates:

```kotlin
interface AppTranslations {
    val locale: String
    fun welcome(name: String): String
    val home: HomeTranslations
    
    interface HomeTranslations {
        val title: String
    }
}

object ENTranslations : AppTranslations {
    override val locale = "en"
    override fun welcome(name: String) = "Welcome $name!"
    override val home = object : AppTranslations.HomeTranslations {
        override val title = "Home"
    }
}
```

## Dependencies

- **Gradle API**: For plugin development
- **KotlinPoet**: For generating Kotlin code
- **kotlinx-serialization-json**: For parsing JSON files
- **kaml**: For parsing YAML files

## Task

The plugin registers a `generateTranslations` task that:
- Runs before Kotlin compilation
- Can be executed manually: `./gradlew generateTranslations`
- Outputs generated files to the configured output directory
