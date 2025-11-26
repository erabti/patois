package com.erabti.patois.plugin.utils

import com.erabti.patois.models.LocalizationConfig
import com.erabti.patois.util.toPascalCase


internal val LocalizationConfig.pascalCaseTag
    get() = toLanguageTag().lowercase().split("-").joinToString("") { it.toPascalCase() }

internal val LocalizationConfig.enumCaseTag get() = toLanguageTag("_").uppercase()
