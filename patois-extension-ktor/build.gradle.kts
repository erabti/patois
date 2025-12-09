plugins {
    alias(libs.plugins.kotlinJvm)
    `java-library`
    alias(libs.plugins.dokka)
}

dependencies {
    api(project(":patois-core"))
    implementation(libs.ktor.server.core)
}

tasks.register<Jar>("javadocJar") {
    dependsOn(tasks.named("dokkaJavadoc"))
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaJavadoc"))
}

afterEvaluate {
    publishing.publications.withType<MavenPublication>().configureEach {
        artifact(tasks.named("javadocJar"))
    }
}
