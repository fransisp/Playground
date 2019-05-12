package org.openapitools.server.database

import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.openapitools.server.dao.*
import org.openapitools.server.dao.Employees.empid
import org.openapitools.server.main

class ExposedDAOTest {
    @Test
    fun `can do simple h2 database ops`() {
        Database.connect("jdbc:h2:mem:test;IGNORECASE=TRUE", driver = "org.h2.Driver")

        transaction {
            addLogger(StdOutSqlLogger)

            SchemaUtils.create(Employees, Departments, Branches)

            val branchTest = BranchDao.new {
                name = "Aperture"
                lead = "GLaDOS"
                description = "is anybody safe?"
            }

            val deptTest = DepartmentDAO.new {
                name = "Research"
                lead = "mr. nobody"
                description = "cool things happens here"
                branch = branchTest
            }

            val empTest = EmployeeDAO.new {
                name = "john doe"
                empid = "john.doe"
                email = "johndoe@acme.io"
                jobtitle = "developer"
                phone = "0123456789"
                department = deptTest
            }

            assert(BranchDao.findById(1)?.equals(branchTest) ?: false)

            assert(DepartmentDAO.findById(1)?.equals(deptTest) ?: false)

            EmployeeDAO.find(Op.build { empid.eq(empTest.name) })
                    .forEach {
                        assertThat(it == empTest)
                    }

            println("Branches: ${BranchDao.all().joinToString { it.name }}")
            println("Departments in ${branchTest.name}: ${DepartmentDAO.all().joinToString { it.name }}")
            println("Employees in ${deptTest.name}: ${deptTest.employees.joinToString { it.name }}")
            testRequest(HttpMethod.Get, "/employee/${empTest.name}") {println(response.status())}
        }
    }
}

private fun testRequest(
        method: HttpMethod,
        uri: String,
        setup: suspend TestApplicationRequest.() -> Unit = {},
        checks: suspend TestApplicationCall.() -> Unit
) {
    httpBinTest {
        val req = handleRequest(method, uri) { runBlocking { setup() } }
        checks(req)
    }
}

private fun httpBinTest(callback: suspend TestApplicationEngine.() -> Unit): Unit {
    withTestApplication(Application::main) {
        runBlocking { callback() }
    }
}