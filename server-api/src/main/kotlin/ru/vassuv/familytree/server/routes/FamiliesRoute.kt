package ru.vassuv.familytree.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import ru.vassuv.familytree.request.FamilyCreateRequest
import ru.vassuv.familytree.server.services.FamilyService


internal fun Routing.familiesRoute(familyService: FamilyService) {
    route("/families") {
        get {
            call.respond(familyService.getFamilies())
        }
        post {
            familyService.addFamily(call.receive<FamilyCreateRequest>())
            call.respond(HttpStatusCode.Created)
        }
    }
}
