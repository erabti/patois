package io.github.erabti.patois.plugin.runners

import io.github.erabti.patois.models.PatoisConfig
import io.github.erabti.patois.plugin.application.runners.GenerateTranslationRunner
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class KtorExtensionTest : FunSpec({
    fun generate(hasKtorExtension: Boolean): String {
        val input = tempdir()
        val output = tempdir()
        input.resolve("en.yaml").writeText("greeting: Hello")

        GenerateTranslationRunner(
            config = PatoisConfig(
                className = "AppStrings",
                packageName = "com.example",
                inputDir = input.absolutePath,
                outputDir = output.absolutePath,
                baseLocale = "en",
                enumName = "AppLocale",
                resolverName = "AppLocaleResolver",
                hasKtorExtension = hasKtorExtension,
            ),
            logger = {},
        ).run()

        return output.resolve("com/example/AppStrings.kt").readText()
    }

    test("a project without the Ktor extension gets no Ktor code") {
        val generated = generate(hasKtorExtension = false)

        generated shouldNotContain "io.ktor"
        generated shouldNotContain "ApplicationCall"
        generated shouldNotContain "RoutingContext"
    }

    test("a project with the Ktor extension gets the strings extensions") {
        val generated = generate(hasKtorExtension = true)

        generated shouldContain "val ApplicationCall.strings: AppStrings"
        generated shouldContain "val RoutingContext.strings: AppStrings"
    }
})
