package org.openapitools.server.ktor

import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.server.testing.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.openapitools.server.main

@KtorExperimentalAPI
private fun testRequest(
        method: HttpMethod,
        uri: String,
        setup: suspend TestApplicationRequest.() -> Unit = {},
        checks: suspend TestApplicationCall.() -> Unit
) {
    httpBinTest {

        val req = handleRequest(method, uri) {
            addHeader("Authorization", "user1")
            runBlocking { setup() } }
        checks(req)
    }
}

@KtorExperimentalAPI
private fun httpBinTest(callback: suspend TestApplicationEngine.() -> Unit) {
    withTestApplication(Application::main) {
        runBlocking { callback() }
    }
}

class RoutingTest {

    @Test
    fun `simple routing test`() {
        testRequest(HttpMethod.Get, "/employee/Test") { println(response.status()) }
    }
}