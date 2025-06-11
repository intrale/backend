package ar.com.intrale

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.withTestApplication
import io.ktor.server.testing.handleRequest
import kotlin.test.Test
import kotlin.test.assertEquals

class HealthEndpointTest {
    @Test
    fun healthReturnsOk() {
        withTestApplication({
            routing {
                get("/health") { call.respondText("OK") }
            }
        }) {
            handleRequest(HttpMethod.Get, "/health").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("OK", response.content)
            }
        }
    }
}
