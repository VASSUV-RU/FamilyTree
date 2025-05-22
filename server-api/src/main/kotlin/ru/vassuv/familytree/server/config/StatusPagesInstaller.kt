package ru.vassuv.familytree.server.config

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import ru.vassuv.familytree.server.ConflictException
import ru.vassuv.familytree.server.ForbiddenException
import ru.vassuv.familytree.server.NotFoundException
import ru.vassuv.familytree.server.UnauthorizedException
import ru.vassuv.familytree.server.ValidationException

class StatusPagesInstaller: (Application) -> Unit {
    override fun invoke(app: Application) {
        app.install(StatusPages) {
            exception<ValidationException> { call, cause ->
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to cause.message))
            }
            exception<ConflictException> { call, cause ->
                call.respond(HttpStatusCode.Conflict, mapOf("error" to cause.message))
            }
            exception<NotFoundException> { call, cause ->
                call.respond(HttpStatusCode.NotFound, mapOf("error" to cause.message))
            }
            exception<UnauthorizedException> { call, cause ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to cause.message))
            }
            exception<ForbiddenException> { call, cause ->
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to cause.message))
            }

            // fallback
            exception<Throwable> { call, cause ->
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (cause.message ?: "Unknown error"))
                )
            }
        }
    }
}