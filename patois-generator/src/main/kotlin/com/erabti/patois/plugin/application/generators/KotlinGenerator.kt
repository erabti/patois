package com.erabti.patois.plugin.application.generators

import com.erabti.patois.plugin.models.PatoisConfig
import com.erabti.patois.plugin.models.TranslationNode
import com.erabti.patois.plugin.utils.Constants
import com.erabti.patois.util.toPascalCase
import com.squareup.kotlinpoet.*

class KotlinGenerator(val config: PatoisConfig) {
    private val packageName = config.packageName
    private val baseAppStringsClassName = ClassName(
        Constants.PATOIS_CORE_PACKAGE, "BaseAppStrings"
    )

    fun generate(fileName: String, nodes: List<TranslationNode>) = FileSpec.builder(packageName, fileName).run {
        addSupress("RedundantVisibilityModifier")

        val rootAbstractClass = generateAbstractClass(
            className = getClassName(fileName),
            nodes = nodes,
            root = true,
        )

        addType(rootAbstractClass.build())

        return@run build()
    }

    fun generateAbstractClass(
        className: ClassName,
        nodes: List<TranslationNode>,
        root: Boolean = false,
    ): TypeSpec.Builder = TypeSpec.classBuilder(className).addModifiers(KModifier.ABSTRACT).apply {
        if (root) {
            superclass(baseAppStringsClassName)
        }

        for (node in nodes) {
            when (node) {
                is TranslationNode.LeafNode -> generateLeafNode(node, this)
                is TranslationNode.MapNode -> {
                    val nestedClassName = getClassName("$className${node.key.toPascalCase()}")

                    val propertySpec = PropertySpec.builder(
                        node.key, nestedClassName
                    ).addModifiers(KModifier.ABSTRACT).build()

                    addProperty(propertySpec)

                    val nestedClassBuilder = generateAbstractClass(nestedClassName, node.children)
                    addType(nestedClassBuilder.build())
                }

                else -> {
                    TODO()
                }
            }
        }
    }


    fun generateLeafNode(node: TranslationNode.LeafNode, builder: TypeSpec.Builder) = builder.apply {
        return if (node.arguments.isNotEmpty()) addFunction(generateLeafNodeFunction(node))
        else addProperty(generateLeafNodeProperty(node))
    }

    fun generateLeafNodeFunction(node: TranslationNode.LeafNode) =
        FunSpec.builder(node.key).addModifiers(KModifier.ABSTRACT).returns(String::class).apply {
            for (arg in node.arguments) {
                addParameter(arg.name, String::class)
            }
        }.build()


    fun generateLeafNodeProperty(node: TranslationNode.LeafNode) =
        PropertySpec.builder(node.key, String::class).addModifiers(KModifier.ABSTRACT).build()

    fun getClassName(className: String) = ClassName("", className)
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
