package org.openapitools.server.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.openapitools.server.dao.*
import org.openapitools.server.dao.Employees.empid
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@Container
val container = PostgreSQLContainer<Nothing>("postgres:12-alpine")
        .apply {
            withUsername("sa")
            withPassword("sa")
            withExposedPorts(5432)
        }

@Testcontainers
class ExposedDAOTest {
    @Test
    fun `can do simple h2 database ops`() {
        container.start();
        Database.connect(url = container.jdbcUrl, driver = "org.postgresql.Driver",
                 user = "sa", password = "sa")

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
        }
        container.stop()
    }
}