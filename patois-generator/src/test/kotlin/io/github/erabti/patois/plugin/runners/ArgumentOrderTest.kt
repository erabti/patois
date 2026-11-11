package io.github.erabti.patois.plugin.runners

import io.github.erabti.patois.models.PatoisConfig
import io.github.erabti.patois.plugin.application.runners.GenerateTranslationRunner
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.string.shouldContain
import java.io.File

class ArgumentOrderTest : FunSpec({
    fun generate(vararg files: Pair<String, String>): File {
        val input = tempdir()
        val output = tempdir()
        files.forEach { (name, content) -> input.resolve(name).writeText(content) }

        GenerateTranslationRunner(
            config = PatoisConfig(
                className = "AppStrings",
                packageName = "com.example",
                inputDir = input.absolutePath,
                outputDir = output.absolutePath,
                baseLocale = "en",
                enumName = "AppLocale",
                resolverName = "AppLocaleResolver",
            ),
            logger = {},
        ).run()

        return output.resolve("com/example")
    }

    test("a locale that reorders its arguments keeps the base locale's parameter order") {
        val generated = generate(
            "en.yaml" to """range: "Between {min} and {max}"""",
            "ar.yaml" to """range: "حتى {max} من {min}"""",
        )

        generated.resolve("AppStrings.kt").readText() shouldContain "fun range(min: String, max: String): String"
        generated.resolve("AppStringsAr.kt").readText() shouldContain "fun range(min: String, max: String): String"
    }

    test("a nested text keeps the base locale's parameter order") {
        val generated = generate(
            "en.yaml" to "cart:\n  limit: \"From {min} to {max}\"",
            "ar.yaml" to "cart:\n  limit: \"إلى {max} من {min}\"",
        )

        generated.resolve("AppStringsAr.kt").readText() shouldContain "fun limit(min: String, max: String): String"
    }

    test("a locale with different arguments keeps its own, so the compiler still reports it") {
        val generated = generate(
            "en.yaml" to """range: "Between {min} and {max}"""",
            "ar.yaml" to """range: "حتى {max}"""",
        )

        generated.resolve("AppStringsAr.kt").readText() shouldContain "fun range(max: String): String"
    }
})
