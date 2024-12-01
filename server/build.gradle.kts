import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
    alias(libs.plugins.shadow)
}

group = "ru.vassuv.familytree"
version = "1.0.0"

application {
    mainClass.set("ru.vassuv.familytree.server.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("shadow")
    archiveClassifier.set("")
    archiveVersion.set("")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.ktor.jackson.client)
    implementation(libs.ktor.client.jackson)
    implementation(libs.kotlin.stdlib)
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
}