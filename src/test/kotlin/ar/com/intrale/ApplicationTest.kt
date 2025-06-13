package ar.com.intrale

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.kodein.di.ktor.closestDI
import org.kodein.di.ktor.di
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.singleton
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    private class HelloFunction : Function {
        override suspend fun execute(business: String, function: String, headers: Map<String, String>, textBody: String): Response {
            return Response(HttpStatusCode.Created)
        }
    }

    @Test
    fun healthEndpointReturnsUp() = testApplication {
        val module = DI.Module("test") {
            bind<org.slf4j.Logger>() with singleton { LoggerFactory.getLogger("test") }
            bind<Config>() with singleton { Config(setOf("biz"), "us-east-1", "pool", "client") }
        }
        application {
            di { import(module) }
            healthRoute()
        }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("{\"status\":\"UP\"}", response.bodyAsText())
    }

    @Test
    fun postExecutesFunction() = testApplication {
        val module = DI.Module("test") {
            bind<org.slf4j.Logger>() with singleton { LoggerFactory.getLogger("test") }
            bind<Config>() with singleton { Config(setOf("biz"), "us-east-1", "pool", "client") }
            bind<Function>(tag = "hello") with singleton { HelloFunction() }
        }
        application {
            di { import(module) }
            routing {
                post("/{business}/{function}") {
                    val di = closestDI()
                    val businessName = call.parameters["business"]
                    val functionName = call.parameters["function"]
                    var functionResponse: Response
                    if (businessName == null) {
                        functionResponse = RequestValidationException("No business defined on path")
                    } else {
                        val config = di.direct.instance<Config>()
                        if (!config.businesses.contains(businessName)) {
                            functionResponse = ExceptionResponse("Business not avaiable with name $businessName")
                        } else {
                            if (functionName == null) {
                                functionResponse = RequestValidationException("No function defined on path")
                            } else {
                                try {
                                    val function = di.direct.instance<Function>(tag = functionName)
                                    val headers: Map<String, String> = call.request.headers.entries().associate { it.key to it.value.joinToString(",") }
                                    functionResponse = function.execute(businessName, functionName, headers, call.receiveText())
                                } catch (e: DI.NotFoundException) {
                                    functionResponse = ExceptionResponse("No function with name $functionName found")
                                }
                            }
                        }
                    }
                    call.respondText(
                        text = com.google.gson.Gson().toJson(functionResponse),
                        contentType = ContentType.Application.Json,
                        status = functionResponse.statusCode
                    )
                }
            }
        }
        val response = client.post("/biz/hello") {
            setBody("{}")
            header(HttpHeaders.ContentType, "application/json")
        }
        assertEquals(HttpStatusCode.Created, response.status)
    }
}
