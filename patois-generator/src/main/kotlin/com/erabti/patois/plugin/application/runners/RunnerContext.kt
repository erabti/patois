package com.erabti.patois.plugin.application.runners

import com.erabti.patois.models.PatoisConfig
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName

internal data class RunnerContext(
    val config: PatoisConfig,
    val translations: List<LocaleTranslation>,
    val baseTranslation: LocaleTranslation,
) {
    val baseTranslationClassName = ClassName(config.packageName, config.className)

    val enumClassName = ClassName(config.packageName, config.enumName)

    val enumListClassName = List::class.asClassName().parameterizedBy(enumClassName)
}