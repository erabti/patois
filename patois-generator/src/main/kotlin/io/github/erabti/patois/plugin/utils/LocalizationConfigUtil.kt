package io.github.erabti.patois.plugin.utils

import io.github.erabti.patois.models.LocalizationConfig
import io.github.erabti.patois.util.toPascalCase


internal val LocalizationConfig.pascalCaseTag
    get() = toTag().lowercase().split("-").joinToString("") { it.toPascalCase() }

internal val LocalizationConfig.enumCaseTag get() = toTag("_").uppercase()
