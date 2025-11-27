package io.github.erabti.patois.models

data class PatoisConfig(
    val className: String,
    val packageName: String,
    val inputDir: String,
    val outputDir: String,
    val argumentPattern: ArgumentPattern = ArgumentPattern.CURLY_BRACES,
    val baseLocale: String?,
    val enumName: String,
    val resolverName: String,
    val hasKtorExtension: Boolean = false,
) {
    enum class ArgumentPattern {
        CURLY_BRACES, PRINTF_STYLE;

        fun wrapWithPattern(argumentName: String): String {
            return when (this) {
                CURLY_BRACES -> "{$argumentName}"
                PRINTF_STYLE -> "%$argumentName"
            }
        }

        companion object {
            fun fromString(value: String): ArgumentPattern {
                return when (value.uppercase()) {
                    "CURLY_BRACES" -> CURLY_BRACES
                    "PRINTF_STYLE" -> PRINTF_STYLE
                    else -> throw IllegalArgumentException("Unknown ArgumentPattern: $value")
                }
            }
        }
    }
}
