package com.erabti.patois.plugin.application.generators

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec

fun FileSpec.Builder.addSupress(warning: String): FileSpec.Builder {
    return this.addAnnotation(
        AnnotationSpec.builder(Suppress::class).addMember("%S", warning).build()
    )
}