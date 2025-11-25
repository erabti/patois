package com.erabti.patois.plugin.models

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property


abstract class PatoisPluginExtension {
    abstract val className: Property<String>
    abstract val inputDir: DirectoryProperty
    abstract val outputDir: DirectoryProperty

}
