package dam.adrianoliva.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.AccessControlAllowHeaders)
        allowHeader(HttpHeaders.AccessControlAllowMethods)
        allowHeader(HttpHeaders.AccessControlAllowCredentials)
        allowHeader(HttpHeaders.AccessControlExposeHeaders)
        allowHeader(HttpHeaders.AccessControlRequestHeaders)
        allowHeader(HttpHeaders.AccessControlRequestMethod)
        allowHeader(HttpHeaders.Origin)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.AcceptLanguage)
        allowHeader(HttpHeaders.Host)
        allowHeader(HttpHeaders.UserAgent)
        allowCredentials = true
        anyHost()
        exposeHeader("key")
    }
}