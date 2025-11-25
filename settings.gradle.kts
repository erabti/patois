pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "patois"

include(":patois-core")
include(":patois-generator")
include(":patois-extension-compose")
include(":patois-extension-ktor")
