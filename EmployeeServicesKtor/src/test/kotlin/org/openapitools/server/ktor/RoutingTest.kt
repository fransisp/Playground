package org.openapitools.server.ktor

import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.openapitools.server.main
import org.openapitools.server.model.Branches
import org.openapitools.server.model.Departments
import org.openapitools.server.model.Employees
import org.openapitools.server.updateJdbcURL
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

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
            runBlocking { setup() }
        }
        checks(req)
    }
}

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
private fun httpBinTest(callback: suspend TestApplicationEngine.() -> Unit) {
    withTestApplication(Application::main) {
        runBlocking { callback() }
        cleanUpAfter()
    }
}

private fun cleanUpAfter() =
        transaction {
            SchemaUtils.drop(Employees, Departments, Branches)
        }

@Testcontainers
class RoutingTest {
    companion object {
        @Container
        val container = PostgreSQLContainer<Nothing>("postgres:12-alpine")
                .apply {
                    withUsername("postgres")
                    withPassword("postgres")
                }

        @BeforeAll
        @JvmStatic
        fun startDBContainer() {
            container.start()
            updateJdbcURL(container.jdbcUrl)
        }

        @AfterAll
        @JvmStatic
        fun stopDBContainer() {
            container.stop()
        }
    }

    @Test
    @KtorExperimentalAPI
    @KtorExperimentalLocationsAPI
    fun `employee api test`() {
        testRequest(HttpMethod.Get, "/employee/john doe") {
            assertEquals(response.status(), HttpStatusCode.OK)
            println(response.content)
        }
    }

    @Test
    @KtorExperimentalAPI
    @KtorExperimentalLocationsAPI
    fun `employee api test not found`() {
        testRequest(HttpMethod.Get, "/employee/TestNotFound") {
            assertEquals(response.status(), HttpStatusCode.NotFound)
            println(response.status())
        }
    }

    @Test
    @KtorExperimentalAPI
    @KtorExperimentalLocationsAPI
    fun `dept api test`() {
        testRequest(HttpMethod.Get, "/organisation/1/department/1") {
            assertEquals(response.status(), HttpStatusCode.OK)
            println(response.content)
        }
    }

    @Test
    @KtorExperimentalAPI
    @KtorExperimentalLocationsAPI
    fun `dept api test not found`() {
        testRequest(HttpMethod.Get, "/organisation/-1/department/-1") {
            assertEquals(response.status(), HttpStatusCode.NotFound)
            println(response.status())
        }
    }

    @Test
    @KtorExperimentalAPI
    @KtorExperimentalLocationsAPI
    fun `branch api test`() {
        testRequest(HttpMethod.Get, "/organisation/1") {
            assertEquals(response.status(), HttpStatusCode.OK)
            println(response.content)
        }
    }

    @Test
    @KtorExperimentalAPI
    @KtorExperimentalLocationsAPI
    fun `branch api test not found`() {
        testRequest(HttpMethod.Get, "/organisation/-1") {
            assertEquals(response.status(), HttpStatusCode.NotFound)
            println(response.status())
        }
    }
}