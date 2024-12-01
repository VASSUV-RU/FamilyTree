package ru.vassuv.familytree.server

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.routing.get
import io.ktor.server.request.*
import io.ktor.server.response.*
import ru.vassuv.familytree.Greeting
import ru.vassuv.familytree.User

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        jackson()
    }
    configureRouting()
}

fun Application.configureRouting() {
    val userService = UserService()
    routing {
        route("/") {
            get {
                call.respondText("Ktor: ${Greeting().greet()}")
            }
        }
        route("/test") {
            get {
                call.respondText("Hello World test!")
            }
        }
        route("/users") {
            get {
                runCatching {
                    call.respond(userService.getUsers())
                }.exceptionOrNull()?.let {
                    call.respondText(it.message ?: it.stackTraceToString())
                }
            }
            post {
                val user = call.receive<User>()
                userService.addUser(user)
                call.respond(HttpStatusCode.Created)
            }
        }
    }
}

class UserService {
    private val users = mutableListOf(
        User(1L, "test", "test@a.b"),
        User(2L, "dev", "dev@a.b"),
        User(3L, "manager", "manager@a.b"),
    )

    fun getUsers(): List<User> {
        return users
    }

    fun addUser(user: User) {
        users.add(user)
    }
}