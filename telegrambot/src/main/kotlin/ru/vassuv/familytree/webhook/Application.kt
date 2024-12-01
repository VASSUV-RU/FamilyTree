package ru.vassuv.familytree.webhook

/*
import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import ru.vassuv.familytree.webhooks.Consts
import java.util.concurrent.TimeUnit

fun main() {
    embeddedServer(Netty, port = 8082) {
        routing {
            post("/webhook") {
                val payload = call.receiveText()
                handleWebhook(payload)
                call.respond(HttpStatusCode.OK)
            }
        }
    }.start(wait = true)
}

fun handleWebhook(payload: String) {
    println("webhookPayload: $payload")

    val process = ProcessBuilder("curl",
        "--location", "https://api.telegram.org/bot${Consts.TELEGRAM_WEBHOOK_BOT_KEY}/sendMessage",
        "--header", "Content-Type: application/json",
        "--data", "{\"chat_id\": \"${Consts.TELEGRAM_BOT_CHANNEL_ID}\", \"text\": \"(Webhook) Ваше сообщение в канал\"}"
    ).start()
    process.inputStream.reader(Charsets.UTF_8).use {
        println(it.readText())
    }
    process.waitFor(10, TimeUnit.SECONDS)
}
*/

// gradle

/*
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "ru.vassuv.familytree"
version = "1.0.0"
application {
    mainClass.set("ru.vassuv.familytree.webhooks.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

dependencies {
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
*/