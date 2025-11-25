package com.erabti.patois.plugin.utils

import com.squareup.kotlinpoet.ClassName

object Constants {
    internal const val DEFAULT_CLASS_NAME = "AppStrings"
    internal const val DEFAULT_INPUT_DIR = "src/main/resources/translations"
    internal const val DEFAULT_OUTPUT_DIR = "generated/source/patois/main/kotlin"

    internal const val PATOIS_CORE_PACKAGE = "com.erabti.patois.models"

    internal val BASE_APP_STRINGS_CLASS_NAME = ClassName(PATOIS_CORE_PACKAGE, "BaseAppStrings")
}