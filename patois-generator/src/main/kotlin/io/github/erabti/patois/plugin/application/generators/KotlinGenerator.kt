package io.github.erabti.patois.plugin.application.generators

import io.github.erabti.patois.models.AppLocaleEnum
import io.github.erabti.patois.models.LocaleResolver
import io.github.erabti.patois.models.LocalizationConfig
import io.github.erabti.patois.plugin.application.runners.RunnerContext
import io.github.erabti.patois.plugin.models.TranslationNode
import io.github.erabti.patois.plugin.models.outputDirFile
import io.github.erabti.patois.plugin.utils.enumCaseTag
import io.github.erabti.patois.plugin.utils.pascalCaseTag
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

            generateFile(config.outputDirFile, className) {
                addTranslationClass(
                    className, nodes, abstractClassName, concreteBuilder
                )
            }
        }
    }

    internal fun generateBaseFile() {
        generateFile(context.config.outputDirFile, baseFileName) {
            addBaseTranslationClass()
            addLocalesEnum()
            addResolverClassObject()
            addKtorStringsExtension()
            addKtorStringsExtension(ClassName("io.ktor.server.routing", "RoutingContext"))
        }
    }

    /** Add this global level property extension to the file
     * val RoutingCall.strings get() = strings<AppStrings>(AppLocale.resolver)
     */
    private fun FileSpec.Builder.addKtorStringsExtension(
        extensionBaseClassName: ClassName = ClassName("io.ktor.server.application", "ApplicationCall"),
    ): FileSpec.Builder {
        val stringsFunctionName = "strings"

        val extensionProperty = PropertySpec.builder("strings", baseTranslationClassName).apply {
            receiver(extensionBaseClassName)
            addModifiers(KModifier.PUBLIC)

            getter(FunSpec.getterBuilder().apply {
                addModifiers(KModifier.INLINE)
                addCode(
                    CodeBlock.builder().apply {
                        addStatement(
                            "return %N<%T>(%T.resolver)",
                            stringsFunctionName,
                            baseTranslationClassName,
                            enumClassName,
                        )
                    }.build()
                )
            }.build())
        }.build()

        addProperty(extensionProperty)
        addImport("io.github.erabti.patois.ktor", stringsFunctionName)
        addImport(extensionBaseClassName.packageName, extensionBaseClassName.simpleName)

        return this
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

    private fun FileSpec.Builder.addResolverClassObject(): FileSpec.Builder {
        val clazz = TypeSpec.objectBuilder(context.config.resolverName).run {
            val localResolverClassName = LocaleResolver::class.asClassName().parameterizedBy(
                baseTranslationClassName,
                enumClassName,
            )

            addSuperinterface(localResolverClassName)

            val defaultLocaleProperty = PropertySpec.builder(
                "defaultLocale",
                enumClassName,
            ).run {
                addModifiers(KModifier.OVERRIDE)
                initializer("%N.%N", context.config.enumName, context.baseTranslation.locale.enumCaseTag)
                build()
            }
            addProperty(defaultLocaleProperty)

            val supportedLocalesProperty = PropertySpec.builder(
                "supportedLocales",
                List::class.asClassName().parameterizedBy(enumClassName),
            ).run {
                addModifiers(KModifier.OVERRIDE)
                initializer("%N.entries", context.config.enumName)
                build()
            }
            addProperty(supportedLocalesProperty)

            val configsMapProperty = PropertySpec.builder(
                "configsMap", Map::class.asClassName().parameterizedBy(
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
            addProperty(configsMapProperty)

            val resolveFunction = FunSpec.builder("resolve").apply {
                addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                returns(enumClassName)
                addParameter(
                    ParameterSpec.builder(
                        "tag",
                        String::class.asClassName().copy(nullable = true),
                    ).build()
                )




                addCode(buildCodeBlock {
                    // Check if localeTag is null, then return DEFAULT
                    addStatement("if (tag == null) return defaultLocale")
                    addStatement(
                        "return %N.findNearestLocale(tag, %N.%N)",
                        "configsMap",
                        context.config.enumName,
                        context.baseTranslation.locale.enumCaseTag,
                    )
                })
                addImport(LocalizationConfig::class.java.packageName, "findNearestLocale")
            }

            addFunction(resolveFunction.build())



            build()
        }

        addType(clazz)
        return this
    }

    private fun FileSpec.Builder.addLocalesEnum(): FileSpec.Builder {
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
                addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
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

            val companion = TypeSpec.companionObjectBuilder().apply {
                // val DEFAULT = resolver.defaultLocale
                val defaultLocaleProperty = PropertySpec.builder(
                    "DEFAULT",
                    enumClassName,
                ).apply {
                    addModifiers(KModifier.PUBLIC)
                    initializer("resolver.defaultLocale")
                }.build()

                val resolverProperty = PropertySpec.builder(
                    "resolver",
                    ClassName(context.config.packageName, context.config.resolverName),
                ).apply {
                    addModifiers(KModifier.PUBLIC)
                    initializer("%N", context.config.resolverName)
                }.build()

                addProperties(listOf(resolverProperty, defaultLocaleProperty))
            }

            addType(companion.build())

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

