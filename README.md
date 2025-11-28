# Patois

[![Maven Central](https://img.shields.io/maven-central/v/io.github.erabti.patois/patois-core)](https://central.sonatype.com/search?q=io.github.erabti.patois)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

Type-safe internationalization (i18n) library and Gradle plugin for Kotlin Multiplatform projects.

## Features

- **Type-safe translations**: Generates Kotlin classes from YAML translation files
- **Compile-time safety**: Missing translations and typos caught at compile time
- **Kotlin Multiplatform**: Supports JVM, iOS (arm64, x64, simulator), and JS (browser, Node.js)
- **Parameter interpolation**: `{param}` style arguments become function parameters
- **Nested keys**: Organize translations hierarchically with nested YAML maps
- **Ktor integration**: Optional server-side locale resolution from HTTP headers

## Installation

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
```

```kotlin
// build.gradle.kts
plugins {
    id("io.github.erabti.patois") version "0.1.0"
}

dependencies {
    implementation("io.github.erabti.patois:patois-core:0.1.0")

    // Optional: Ktor server integration
    implementation("io.github.erabti.patois:patois-extension-ktor:0.1.0")
}
```

## Quick Start

### 1. Create translation files

Create YAML files in `src/main/resources/translations/`:

```yaml
# en.yaml (or en.i18n.yaml)
greeting: Hello
welcome: "Welcome, {name}!"
errors:
  notFound: Page not found
  unauthorized: Access denied
```

```yaml
# de.yaml
greeting: Hallo
welcome: "Willkommen, {name}!"
errors:
  notFound: Seite nicht gefunden
  unauthorized: Zugriff verweigert
```

### 2. Configure the plugin (optional)

```kotlin
// build.gradle.kts
patois {
    className = "AppStrings"           // Default: "AppStrings"
    packageName = "com.example.i18n"   // Default: auto-detected from source
    baseLocale = "en"                  // Default: first file found
}
```

### 3. Use generated code

After running `./gradlew generateTranslations` (or any compile task):

```kotlin
import com.example.i18n.AppStrings
import com.example.i18n.AppStringsEn
import com.example.i18n.AppStringsDe
import com.example.i18n.AppLocale

// Direct usage
val strings: AppStrings = AppStringsEn()
println(strings.greeting)              // "Hello"
println(strings.welcome("World"))      // "Welcome, World!"
println(strings.errors.notFound)       // "Page not found"

// Using the locale enum
val locale = AppLocale.EN
val localizedStrings = locale.toStrings()

// Using the resolver (finds nearest match for locale tag)
val resolved = AppLocale.resolver("en-US")  // Returns AppLocale.EN
```

## Configuration Options

| Option | Description | Default |
|--------|-------------|---------|
| `className` | Generated interface/class name | `"AppStrings"` |
| `packageName` | Package for generated code | Auto-detected from source |
| `inputDir` | Directory with translation YAML files | `"src/main/resources/translations"` |
| `outputDir` | Output directory for generated code | `"build/generated/source/patois/main/kotlin"` |
| `baseLocale` | Locale that defines the interface contract | First file found |
| `argumentPattern` | `CURLY_BRACES` (`{arg}`) or `PRINTF_STYLE` (`%arg`) | `CURLY_BRACES` |
| `enumName` | Generated locale enum name | `"AppLocale"` |
| `resolverName` | Generated resolver object name | `"AppLocaleResolver"` |

## Ktor Integration

For Ktor server applications, add the extension dependency and install the plugin:

```kotlin
import io.github.erabti.patois.ktor.installPatoisPlugin
import com.example.i18n.AppLocale

fun Application.module() {
    // Install with default locale extraction (Accept-Language header)
    installPatoisPlugin(AppLocale.resolver)

    // Or with custom locale extraction
    installPatoisPlugin(AppLocale.resolver) {
        localeExtractor = PatoisLocaleExtractor { call ->
            // Custom logic: check cookie, header, query param, etc.
            call.request.cookies["locale"]
                ?: call.request.headers["Accept-Language"]
        }
    }
}
```

In route handlers, use the generated `strings` extension property:

```kotlin
import com.example.i18n.strings  // Generated extension

routing {
    get("/hello") {
        // Automatically resolves locale from the request
        call.respondText(strings.greeting)
    }

    get("/welcome/{name}") {
        val name = call.parameters["name"] ?: "Guest"
        call.respondText(strings.welcome(name))
    }
}
```

## Generated Code Structure

For translation files `en.yaml` and `de.yaml`, the plugin generates:

| Generated | Description |
|-----------|-------------|
| `AppStrings` | Abstract class defining the translation interface |
| `AppStringsEn` | Implementation for English locale |
| `AppStringsDe` | Implementation for German locale |
| `AppLocale` | Enum with `EN`, `DE` entries |
| `AppLocaleResolver` | Object for resolving locale tags to enum values |
| `ApplicationCall.strings` | Extension property (when using Ktor extension) |
| `RoutingContext.strings` | Extension property (when using Ktor extension) |

## Modules

| Module | Description | Platforms |
|--------|-------------|-----------|
| `patois-core` | Runtime types and locale resolution | JVM, iOS, JS |
| `patois-generator` | Gradle plugin for code generation | JVM (Gradle) |
| `patois-extension-ktor` | Ktor server integration | JVM |

## Requirements

- Kotlin 2.0+
- Gradle 8.0+
- JVM 21+ (for the Gradle plugin)

## License

```
Copyright 2024 Ahmed Erabti

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
