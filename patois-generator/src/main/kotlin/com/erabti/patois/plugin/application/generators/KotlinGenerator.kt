package com.erabti.patois.plugin.application.generators

import com.erabti.patois.models.AppLocaleEnum
import com.erabti.patois.models.LocalizationConfig
import com.erabti.patois.plugin.application.runners.RunnerContext
import com.erabti.patois.plugin.models.TranslationNode
import com.erabti.patois.plugin.utils.enumCaseTag
import com.erabti.patois.plugin.utils.pascalCaseTag
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File

internal class KotlinGenerator(
    val context: RunnerContext,
) {
    private val packageName = context.config.packageName
    private val baseFileName = context.config.className
    private val baseTranslation = context.baseTranslation
    private val baseTranslationClassName = context.baseTranslationClassName
    val enumClassName = context.enumClassName

    internal fun generateConcreteFiles() {
        val config = context.config
        val abstractClassName = ClassName(config.packageName, config.className)

        for (translation in context.translations) {
            val localization = translation.locale
            val nodes = translation.nodes
            val concreteFlavor = TranslationClassConcreteFlavor(localization, abstractClassName, context)
            val concreteBuilder = TranslationClassSpecBuilder(concreteFlavor)
            val className = "${config.className}${localization.pascalCaseTag}"

            generateFile(config.outputDir, className) {
                addTranslationClass(
                    className, nodes, abstractClassName, concreteBuilder
                )
            }
        }
    }

    internal fun generateBaseFile() {
        generateFile(context.config.outputDir, baseFileName) {
            addBaseTranslationClass()
            addLocalesEnum(context)
        }
    }


    private fun FileSpec.Builder.addBaseTranslationClass() {
        val abstractFlavor = TranslationClassAbstractFlavor(context)
        val abstractTranslationCLassSpecBuilder = TranslationClassSpecBuilder(abstractFlavor)

        addTranslationClass(
            baseFileName, baseTranslation.nodes, null, translationClassSpecBuilder = abstractTranslationCLassSpecBuilder
        )
    }


    private fun generateFile(
        directory: File,
        fileName: String,
        block: FileSpec.Builder.() -> FileSpec.Builder,
    ) = with(this) { block(FileSpec.builder(packageName, fileName)).build().writeTo(directory) }


    private fun FileSpec.Builder.addLocalesEnum(context: RunnerContext): FileSpec.Builder {
        val enum = TypeSpec.enumBuilder(enumClassName).apply {
            addSuperinterface(
                AppLocaleEnum::class.asClassName().parameterizedBy(baseTranslationClassName)
            )

            for (translation in context.translations) {
                addEnumConstant(translation.locale.enumCaseTag)
            }

            addFunction(FunSpec.builder("toStrings").apply {
                addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                returns(baseTranslationClassName)
                val whenSpec = CodeBlock.builder()
                whenSpec.beginControlFlow("return when(this)")
                for (translation in context.translations) {
                    val locale = translation.locale
                    whenSpec.addStatement(
                        "%N.%N -> %T()",
                        context.config.enumName,
                        locale.enumCaseTag,
                        ClassName(context.config.packageName, "${context.config.className}${locale.pascalCaseTag}")
                    )
                }
                whenSpec.endControlFlow()
                addCode(whenSpec.build())
            }.build())

            addProperty(PropertySpec.builder("config", LocalizationConfig::class).apply {
                addModifiers(KModifier.PUBLIC)
                getter(FunSpec.getterBuilder().apply {
                    val whenSpec = CodeBlock.builder()
                    whenSpec.beginControlFlow("return when(this)")
                    for (translation in context.translations) {
                        val locale = translation.locale
                        whenSpec.addStatement(
                            "%N.%N -> %T(languageCode = %S, countryCode = %S, scriptCode = %S)",
                            context.config.enumName,
                            locale.enumCaseTag,
                            LocalizationConfig::class,
                            locale.languageCode,
                            locale.countryCode,
                            locale.scriptCode,
                        )
                    }
                    whenSpec.endControlFlow()
                    addCode(whenSpec.build())
                }.build())
            }.build())

            val companoin = TypeSpec.companionObjectBuilder().apply {
                /**
                 * val configsMap = mapOf(
                 *    AppLocale.EN.config to AppLocale.EN,
                 *    AppLocale.DE.config to AppLocale.DE,
                 *    AppLocale.FR.config to AppLocale.FR,
                 * )
                 */
                val configsMapProperty = PropertySpec.builder(
                    "configsMap",
                    Map::class.asClassName().parameterizedBy(
                        LocalizationConfig::class.asTypeName(),
                        enumClassName,
                    )
                ).apply {
                    initializer(buildCodeBlock {

                        addStatement("mapOf(")
                        for (translation in context.translations) {
                            val locale = translation.locale
                            addStatement(
                                "%N.%N.config to %N.%N,",
                                context.config.enumName,
                                locale.enumCaseTag,
                                context.config.enumName,
                                locale.enumCaseTag,
                            )
                        }
                        addStatement(")")
                    })
                }.build()


                val resolveFunction = FunSpec.builder("resolve").apply {
                    addModifiers(KModifier.PUBLIC)
                    addParameter("localeTag", String::class)
                    returns(enumClassName)
                    /// Should return something like:
                    // return configsMap.findNearestLocale(localeTag, AppLocale.EN)
                    addCode(buildCodeBlock {
                        addStatement(
                            "return %N.findNearestLocale(localeTag, %N.%N)",
                            "configsMap",
                            context.config.enumName,
                            context.baseTranslation.locale.enumCaseTag,
                        )
                    })
                    addImport(LocalizationConfig::class.java.packageName, "findNearestLocale")
                }

                addFunction(resolveFunction.build())
                addProperty(configsMapProperty)
            }

            addType(companoin.build())

        }.build()


        addType(enum)
        return this
    }

    internal fun FileSpec.Builder.addTranslationClass(
        className: String,
        nodes: List<TranslationNode>,
        extendsClass: ClassName? = null,
        translationClassSpecBuilder: TranslationClassSpecBuilder,
    ): FileSpec.Builder {
        addSupress("RedundantVisibilityModifier", "unused", "RemoveRedundantQualifierName")
        val className = ClassName("", className)
        val rootClass =
            translationClassSpecBuilder.build(className = className, nodes = nodes, extendsClass = extendsClass)
        addType(rootClass)

        return this
    }
}


@Suppress("SameParameterValue")
private fun FileSpec.Builder.addSupress(vararg warning: String): FileSpec.Builder {
//    return addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", warning).build())
    return addAnnotation(AnnotationSpec.builder(Suppress::class).apply {
        for (w in warning) {
            addMember("%S", w)
        }
    }.build())
}

