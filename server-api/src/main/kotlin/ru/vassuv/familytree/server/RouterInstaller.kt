package ru.vassuv.familytree.server

import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import ru.vassuv.familytree.Greeting
import ru.vassuv.familytree.server.routes.familiesRoute
import ru.vassuv.familytree.server.routes.usersRoute
import ru.vassuv.familytree.server.services.FamilyService
import ru.vassuv.familytree.server.services.UserService

class RouterInstaller(
    private val userService: UserService,
    private val familyService: FamilyService
): (Application) -> Unit {
    override fun invoke(application: Application) {
        application.routing {
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
            usersRoute(userService)
            familiesRoute(familyService)
        }
    }

}