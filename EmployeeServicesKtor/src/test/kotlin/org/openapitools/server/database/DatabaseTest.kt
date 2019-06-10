package org.openapitools.server.database

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.openapitools.server.dao.*
import org.openapitools.server.dao.Departments.branch
import org.openapitools.server.dao.Employees.department
import org.openapitools.server.dao.Employees.empid
import org.openapitools.server.models.Branch
import org.openapitools.server.service.DatabaseFactory
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

                assert(BranchDao.findById(1)?.name.equals("Aperture"))

                assert(DepartmentDAO.findById(1)?.name.equals("Research"))

                EmployeeDAO.find(Op.build { empid.eq("john doe") })
                        .forEach {
                            assertThat(it.id.value == 1L)
                        }

                BranchDao.all().forEach {
                    println("Branches: ${it.name}")
                    println("Departments in ${it.name}:")
                    DepartmentDAO.find(Op.build { branch.eq(it.id) })
                            .forEach {
                                println("Department: ${it.name}")
                                println("With Employees in ${it.name}:")
                                EmployeeDAO.find(Op.build { department.eq(it.id) })
                                        .forEach { println("Employee: ${it.name}") }
                            }
                }
            }
        }
    }
}