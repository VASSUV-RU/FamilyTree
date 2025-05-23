import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "ru.vassuv.familytree.server"
version = "1.0.0"

application {
    mainClass.set("ru.vassuv.familytree.server.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

sourceSets {
    test {
        kotlin.srcDirs("src/test/kotlin")
        resources.srcDirs("src/test/resources")
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("shadow")
    archiveClassifier.set("")
    archiveVersion.set("")
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(projects.shared)
    implementation(projects.serverApi)
    implementation(projects.serverRepository)
    implementation(projects.serverService)

    implementation(libs.kotlin.stdlib)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)

}