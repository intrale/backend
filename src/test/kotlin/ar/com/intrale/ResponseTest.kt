package ar.com.intrale

import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals

class ResponseTest {
    @Test
    fun defaultStatusIsOk() {
        val resp = Response()
        assertEquals(HttpStatusCode.OK, resp.statusCode)
    }
}
