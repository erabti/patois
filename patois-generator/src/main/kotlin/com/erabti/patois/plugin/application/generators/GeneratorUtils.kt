package com.erabti.patois.plugin.application.generators

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec

internal fun FileSpec.Builder.addSupress(warning: String): FileSpec.Builder {
    return this.addAnnotation(
        AnnotationSpec.builder(Suppress::class).addMember("%S", warning).build()
    )
}

internal fun className(className: String): ClassName {
    return ClassName("", className)
}