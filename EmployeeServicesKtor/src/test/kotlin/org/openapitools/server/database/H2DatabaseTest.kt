package org.openapitools.server.database

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.openapitools.server.dao.*
import org.openapitools.server.dao.Employees.empid
import org.openapitools.server.models.Branch
import org.openapitools.server.models.Employee

class ExposedDAOTest {
    @Test
    fun `can do simple h2 database ops`() {
        Database.connect("jdbc:h2:mem:test;IGNORECASE=TRUE", driver = "org.h2.Driver")

        transaction {
            addLogger(StdOutSqlLogger)

            SchemaUtils.create(Employees, Departments, Branches)

            val branchTest = BranchDao.new { name = "Aparture"
                lead = "GLADOS"
                description = "is anybody safe?"
            }

            val deptTest = DepartmentDAO.new { name = "Research"
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
                    .forEach{
                        assertThat(it == empTest)
                    }

            println("Branches: ${BranchDao.all().joinToString { it.name }}")
            println("Departments in ${branchTest.name}: ${DepartmentDAO.all().joinToString { it.name }}")
            println("Employees in ${deptTest.name}: ${deptTest.employees.joinToString { it.name }}")
        }
    }
}