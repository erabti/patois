package com.erabti.patois.plugin.generators

import com.erabti.patois.plugin.models.PatoisConfig
import com.erabti.patois.plugin.models.TranslationNode
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

class KotlinGenerator(val config: PatoisConfig, val nodes: List<TranslationNode>) {
    val packageName = ""

    fun generate(): FileSpec {
        val file = FileSpec.builder(packageName, "AppStrings").addType(generateInterface().build())
        return file.build()
    }

    fun generateInterface(): TypeSpec.Builder {
        val helloStr = PropertySpec.builder("hello", String::class).build()

        return TypeSpec.classBuilder("AppStrings").superclass(
            ClassName(
                "com.erabti.patois", "BaseAppStrings"
            )
        ).addProperty(helloStr)
    }
}
