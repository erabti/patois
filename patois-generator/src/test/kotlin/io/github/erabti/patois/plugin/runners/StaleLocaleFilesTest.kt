package io.github.erabti.patois.plugin.runners

import io.github.erabti.patois.models.PatoisConfig
import io.github.erabti.patois.plugin.application.runners.GenerateTranslationRunner
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.io.File

class StaleLocaleFilesTest : FunSpec({
    fun generate(input: File, output: File) {
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

    fun File.names() = listFiles().orEmpty().map { it.name }

    test("a removed locale's class is deleted on the next run") {
        val input = tempdir()
        val output = tempdir()
        listOf("en", "ar", "de").forEach { input.resolve("$it.yaml").writeText("greeting: Hello") }
        generate(input, output)

        input.resolve("de.yaml").delete()
        generate(input, output)

        output.resolve("com/example").names() shouldContainExactlyInAnyOrder
            listOf("AppStrings.kt", "AppStringsAr.kt", "AppStringsEn.kt")
    }

    test("a file the generator did not write stays") {
        val input = tempdir()
        val output = tempdir()
        input.resolve("en.yaml").writeText("greeting: Hello")
        val handWritten = output.resolve("com/example").apply { mkdirs() }.resolve("AppStringsHelpers.kt")
        handWritten.writeText("package com.example\n\nfun AppStrings.shout() = greeting.uppercase()\n")

        generate(input, output)

        handWritten.exists() shouldBe true
    }
})
