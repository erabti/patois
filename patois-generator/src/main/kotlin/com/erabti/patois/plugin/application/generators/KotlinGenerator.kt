package com.erabti.patois.plugin.application.generators

import com.erabti.patois.models.AppLocale
import com.erabti.patois.plugin.models.PatoisConfig
import com.erabti.patois.plugin.models.TranslationNode
import com.erabti.patois.plugin.utils.Constants
import com.erabti.patois.util.toPascalCase
import com.squareup.kotlinpoet.*

class KotlinGenerator(val config: PatoisConfig) {
    private val packageName = ""
    private val baseAppStringsClassName = ClassName(
        Constants.PATOIS_CORE_PACKAGE, "BaseAppStrings"
    )

    fun generate(fileName: String, nodes: List<TranslationNode>): FileSpec {
        val rootAbstractClass = generateAbstractClass(
            className = fileName,
            nodes = nodes,
            root = true,
        ).build()

        val file = FileSpec.builder(packageName, fileName).addType(rootAbstractClass)

        return file.build()
    }

    fun generateAbstractClass(
        className: String,
        nodes: List<TranslationNode>,
        root: Boolean = false,
    ): TypeSpec.Builder {
        var builder = TypeSpec.classBuilder(className).addModifiers(KModifier.ABSTRACT)

        fun addSpec(block: TypeSpec.Builder.() -> Unit): TypeSpec.Builder {
            builder = builder.apply(block)
            return builder
        }

        if (root) {
            addSpec { superclass(baseAppStringsClassName) }
        }


        for (node in nodes) {
            when (node) {
                is TranslationNode.LeafNode -> {
                    val isFunction = node.arguments.isNotEmpty()

                    if (isFunction) {
                        val funBuilder =
                            FunSpec.builder(node.key).addModifiers(KModifier.ABSTRACT).returns(String::class)

                        for (arg in node.arguments) {
                            funBuilder.addParameter(arg.name, String::class)
                        }

                        addSpec { addFunction(funBuilder.build()) }
                    } else {
                        val propertySpec =
                            PropertySpec.builder(node.key, String::class).addModifiers(KModifier.ABSTRACT).build()

                        addSpec { addProperty(propertySpec) }
                    }
                }

                is TranslationNode.MapNode -> {
                    val nestedClassName = "$className${node.key.toPascalCase()}"

                    val propertySpec = PropertySpec.builder(
                        node.key, ClassName(packageName, nestedClassName)
                    ).addModifiers(KModifier.ABSTRACT).build()

                    addSpec { addProperty(propertySpec) }

                    val nestedClassBuilder =
                        generateAbstractClass(nestedClassName, node.children)
                    addSpec { addType(nestedClassBuilder.build()) }
                }

                else -> {
                    TODO()
                }
            }
        }

        return builder
    }

    fun generateTranslationClass(
        locale: AppLocale,
        nodes: List<TranslationNode>,
    ) {
        //        val localeProperty = PropertySpec.builder("locale", AppLocale::class).addModifiers(KModifier.OVERRIDE)
    }
}


// AppStrings.kt
//abstract class AppStrings : BaseAppStrings() {
//    abstract val hello: Hello
//
//    abstract class Hello {
//        abstract val mellow: String
//        abstract val bye: Bye
//
//        abstract class Bye {
//            abstract val farewell: String
//        }
//    }
//}
//
//
//class AppStringsEn : AppStrings() {
//    override val locale = AppLocale("en")
//
//    override val hello = HelloAppStringsEn()
//
//
//    class HelloAppStringsEn : Hello() {
//        override val mellow = "Mellow"
//        override val bye = ByeAppStringsEn()
//
//        class ByeAppStringsEn : Bye() {
//            override val farewell = "Farewell"
//        }
//    }
//}
