package ru.vassuv.familytree.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import ru.vassuv.familytree.request.UserCreateRequest
import ru.vassuv.familytree.server.services.UserService

internal fun Routing.usersRoute(userService: UserService) {
    route("/users") {
        get {
            call.respond(userService.getUsers())
        }
        post {
            userService.addUser(call.receive<UserCreateRequest>())
            call.respond(HttpStatusCode.Created)
        }
    }
}