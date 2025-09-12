plugins {
    id("org.springframework.boot")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":app:api"))
    implementation(project(":app:bot-telegram"))
    implementation(project(":app:libs:exceptionhandler"))
    implementation(project(":app:libs:config"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
