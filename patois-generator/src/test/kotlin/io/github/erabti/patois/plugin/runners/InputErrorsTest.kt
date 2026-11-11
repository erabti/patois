package io.github.erabti.patois.plugin.runners

import io.github.erabti.patois.models.PatoisConfig
import io.github.erabti.patois.plugin.application.runners.GenerateTranslationRunner
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class InputErrorsTest : FunSpec({
    fun generate(vararg files: Pair<String, String>) {
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
    }

    test("an empty file fails with the file's name") {
        val error = shouldThrow<IllegalStateException> {
            generate("en.yaml" to "greeting: Hello", "ar.yaml" to "")
        }

        error.message shouldContain "ar.yaml"
        error.message shouldContain "empty"
    }

    test("a file that isn't valid YAML fails with the file's name") {
        val error = shouldThrow<IllegalStateException> {
            generate("en.yaml" to "greeting: Hello", "ar.yaml" to "greeting: [unclosed")
        }

        error.message shouldContain "ar.yaml"
    }

    test("a key with no value fails with the file's name and the key's path") {
        val error = shouldThrow<IllegalStateException> {
            generate("en.yaml" to "cart:\n  title:\n  other: Other")
        }

        error.message shouldContain "en.yaml"
        error.message shouldContain "'cart.title' has no value"
    }

    test("a top-level key named locale fails with the file's name") {
        val error = shouldThrow<IllegalStateException> {
            generate("en.yaml" to "locale: Language")
        }

        error.message shouldContain "en.yaml"
        error.message shouldContain "'locale' can't be a top-level key"
    }

    test("a nested key named locale is allowed") {
        generate("en.yaml" to "settings:\n  locale: Language") shouldBe Unit
    }
})
