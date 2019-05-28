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
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.dao.*

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
        //cleanup schema
        cleanUpAfter()
    }
}

private fun cleanUpAfter() = transaction {
    SchemaUtils.drop(Employees, Departments, Branches)
}

class RoutingTest {
    @Test
    @KtorExperimentalAPI
    @KtorExperimentalLocationsAPI
    fun `simple routing test`() {
        testRequest(HttpMethod.Get, "/employee/john doe") {
            assertEquals(response.status(), HttpStatusCode.OK)
            println(response.content) }
    }

    @Test
    @KtorExperimentalAPI
    @KtorExperimentalLocationsAPI
    fun `routing 500 test`() {
        testRequest(HttpMethod.Get, "/employee/TestNotFound") {
            assertEquals(response.status(), HttpStatusCode.NotFound)
            println(response.status()) }
    }
}