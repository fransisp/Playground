package org.openapitools.server.ktor

import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.openapitools.server.main
import java.util.*
import org.junit.Assert.*
import io.ktor.http.HttpStatusCode

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
private fun testRequest(
        method: HttpMethod,
        uri: String,
        setup: suspend TestApplicationRequest.() -> Unit = {},
        checks: suspend TestApplicationCall.() -> Unit
) {
    httpBinTest {
        val credentials = "admin:admin"
        val auth = "Basic " + Base64.getEncoder().encodeToString(credentials.toByteArray())
        val req = handleRequest(method, uri) {
            addHeader("Content-Type", "application/json")
            addHeader("Authorization", auth)
            runBlocking { setup() } }
        checks(req)
    }
}

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
private fun httpBinTest(callback: suspend TestApplicationEngine.() -> Unit) {
    withTestApplication(Application::main) {
        runBlocking { callback() }
    }
}

class RoutingTest {
    @Test
    @KtorExperimentalAPI
    @KtorExperimentalLocationsAPI
    fun `simple routing test`() {
        testRequest(HttpMethod.Get, "/employee/Test") {
            assertEquals(response.status(), HttpStatusCode.OK)
            println(response.content) }
    }
}