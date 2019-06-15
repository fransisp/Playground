package org.openapitools.server.database

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.assertj.core.api.Assertions.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.openapitools.server.dao.*
import org.openapitools.server.dao.Departments.branch
import org.openapitools.server.dao.Departments.id
import org.openapitools.server.dao.Employees.department
import org.openapitools.server.dao.Employees.empid
import org.openapitools.server.dao.Employees.name
import org.openapitools.server.utils.DatabaseFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class ExposedDAOTest {
    companion object {
        @Container
        val container = PostgreSQLContainer<Nothing>("postgres:12-alpine")
                .apply {
                    withUsername("sa")
                    withPassword("sa")
                    withExposedPorts(5432)
                }

        @BeforeAll
        @JvmStatic
        fun startDBContainer() {
            container.start()


        }

        @AfterAll
        @JvmStatic
        fun stopDBContainer() {
            container.stop()
        }
    }

    @Test
    fun `can do simple h2 database ops`() {
        DatabaseFactory.init(driverName = "org.postgresql.Driver", jdbcURL = container.jdbcUrl, username = "sa", password = "sa")

        runBlocking {
            DatabaseFactory.dbQuery {

                //assert(BranchDao.findById(1)?.name.equals("Aperture"))
                Branches.select {Branches.id eq 1L}.forEach{assert(it[Branches.name] == "Aperture")}
                //assert(DepartmentDAO.findById(1)?.name.equals("Research"))
                Departments.select {Departments.id eq 1L}.forEach{assert(it[Departments.name] == "Research")}

                Employees.select { empid.eq("john doe") }
                        .forEach {
                            assertThat(it[id].value == 1L)
                        }

                Branches.selectAll().forEach {
                    println("Branches: ${it[Branches.name]}")
                    println("Departments in ${it[Branches.name]}:")
                    Departments.select {branch eq it[Branches.id]}
                            .forEach {
                                println("Department: ${it[Departments.name]}")
                                println("With Employees in ${it[Departments.name]}:")
                                Employees.select{ department eq it[id] }.forEach{ println("Employee: ${it[Employees.name]}")}
                            }
                }
            }
        }
    }
}