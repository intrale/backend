package ar.com.intrale

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertTrue

class SecuredFunctionTest {
    class DummySecuredFunction(override val config: Config) : SecuredFunction(config, LoggerFactory.getLogger("test")) {
        var called = false
        override suspend fun securedExecute(business: String, function: String, headers: Map<String, String>, textBody: String): Response {
            called = true
            return Response()
        }
    }

    @Test
    fun invalidTokenReturnsUnauthorized() {
        val cfg = Config(setOf("biz"), "us-east-1", "pool", "client")
        val func = DummySecuredFunction(cfg)
        val resp = runBlocking { func.execute("biz", "func", emptyMap(), "body") }
        assertTrue(resp is UnauthorizeExeption)
        assertTrue(!func.called)
    }
}
