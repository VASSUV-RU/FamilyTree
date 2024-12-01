import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import kotlin.io.readText

plugins {
    alias(libs.plugins.kotlinJvm)
    application
    alias(libs.plugins.shadow)
}

group = "ru.vassuv.familytree.telegram.deploy.bot"
version = Version().incrementedVersion

application {
    mainClass.set("ru.vassuv.familytree.telegrambot.ApplicationKt")
}

tasks.named<ShadowJar>("shadowJar") {
    dependsOn("saveIncrementVersion")
    println("Создался jar файл сборки telegram-bot-server (${Version().incrementedVersion})")
    archiveBaseName.set("${project.name}-${Version().incrementedVersion}")
    archiveClassifier.set("")
    archiveVersion.set("")
}

tasks.register("saveIncrementVersion") {
    Version().apply { saveVersion(incrementedVersion) }
}

dependencies {
    implementation(libs.kotlin.telegram.bot)
}


class Version {
    private val fileName = "telegrambot/version.txt"
    val versionFile = File(fileName).apply { if(!exists()) createNewFile() }
    val lastVersion: String = runCatching { versionFile.readText().ifEmpty { null } }.getOrNull() ?: "0.0.0"
    val incrementedVersion = lastVersion.incrementVersion()
    fun saveVersion(version: String) = versionFile.writeText(version)

    private fun String.incrementVersion(): String {
        val values = split(".")
        return values
            .mapIndexed { i, v -> if (i == values.lastIndex) v.toInt() + 1 else v.toInt() }
            .joinToString(".")
    }
}