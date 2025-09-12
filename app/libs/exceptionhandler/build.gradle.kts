plugins {
    kotlin("jvm") version "2.2.10"

    id("io.spring.dependency-management") version "1.1.7"
//    id("java-library") // ✅ важно для публикации библиотеки
//    id("maven-publish") // ✅ нужно для публикации
}

dependencies {
    implementation("org.springframework.boot:spring-boot-autoconfigure:3.2.5")
    implementation("org.springframework:spring-web:6.1.14")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.0")
}