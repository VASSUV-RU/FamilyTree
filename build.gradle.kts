plugins {
    kotlin("jvm") version "2.2.10" apply true
    kotlin("plugin.spring") version "2.2.10" apply true
    kotlin("plugin.jpa") version "2.2.10" apply true
    id("org.springframework.boot") version "3.5.5" apply true
    id("io.spring.dependency-management") version "1.1.7" apply true
}

allprojects {
    group = "ru.vassuv.familytree"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.spring.dependency-management")

    kotlin {
        jvmToolchain(24)
    }

    dependencies {
        testImplementation(kotlin("test"))
    }

    tasks.test {
        useJUnitPlatform()
    }
}