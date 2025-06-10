package ar.com.intrale

import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals

class UnauthorizeExeptionTest {
    @Test
    fun returnsUnauthorized() {
        val ex = UnauthorizeExeption()
        assertEquals(HttpStatusCode.Unauthorized, ex.statusCode)
    }
}
