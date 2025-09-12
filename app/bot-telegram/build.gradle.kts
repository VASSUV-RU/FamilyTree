plugins {
    id("org.springframework.boot")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":app:service"))
    implementation("org.springframework.boot:spring-boot-starter")

    // Add Telegram bot integration when ready:
    // implementation("org.telegram:telegrambots-spring-boot-starter:6.9.7")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

