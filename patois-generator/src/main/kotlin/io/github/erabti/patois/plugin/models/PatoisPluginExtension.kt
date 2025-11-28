package io.github.erabti.patois.plugin.models

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

/**
 * Configuration extension for the Patois Gradle plugin.
 *
 * Configure in your `build.gradle.kts`:
 * ```kotlin
 * patois {
 *     className = "AppStrings"           // Generated class name (default: "AppStrings")
 *     packageName = "com.example.i18n"   // Package for generated code
 *     inputDir = file("src/main/resources/translations")  // Translation files location
 *     baseLocale = "en"                  // Base locale for the interface
 * }
 * ```
 */
abstract class PatoisPluginExtension {
    /** Name of the generated translation interface/class. Default: "AppStrings" */
    abstract val className: Property<String>

    /** Package name for generated code. Auto-detected from source if not set. */
    abstract val packageName: Property<String>

    /** Directory containing translation YAML/JSON files. Default: "src/main/resources/i18n" */
    abstract val inputDir: DirectoryProperty

    /** Output directory for generated Kotlin files. Default: "build/generated/patois" */
    abstract val outputDir: DirectoryProperty

    /** Base locale (e.g., "en") that defines the translation interface contract. */
    abstract val baseLocale: Property<String>

    /** Pattern for argument interpolation: CURLY_BRACES ({arg}) or PERCENT (%%arg%%). */
    abstract val argumentPattern: Property<String>

    /** Name of the generated locale enum. Default: "AppLocale" */
    abstract val enumName: Property<String>

    /** Name of the generated resolver class. Default: "AppStringsResolver" */
    abstract val resolverName: Property<String>

    /** Internal: Whether patois-extension-ktor is present in dependencies. */
    abstract val hasKtorExtension: Property<Boolean>
}
