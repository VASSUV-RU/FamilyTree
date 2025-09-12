plugins {
    id("org.springframework.boot")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":app:api"))
    implementation(project(":app:bot-telegram"))
    implementation(project(":app:libs:exceptionhandler"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

