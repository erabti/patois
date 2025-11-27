plugins {
    alias(libs.plugins.kotlinJvm)
    id("maven-publish")
}

dependencies {
    api(project(":patois-core"))
    implementation(libs.ktor.server.core)
}

publishing {
    publications {
        create<MavenPublication>("mavenKotlin") {
            from(components["kotlin"])
        }
    }

    repositories {
        mavenLocal()
    }
}
