package org.openapitools.server.service

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.dao.*

object DatabaseFactory {

    fun init(driverName:String, jdbcURL:String, username:String, password:String) {
        Database.connect(hikariConfiguration(driverName, jdbcURL, username, password))
        transaction {
            SchemaUtils.create(Employees, Departments, Branches)
            val branchTest = BranchDao.new {
                name = "Aperture"
                lead = "GLaDOS"
                description = "is anybody safe?"
                location = "unknown"
            }

            val deptTest = DepartmentDAO.new {
                name = "Research"
                lead = "mr. nobody"
                description = "cool things happens here"
                branch = branchTest
            }

            EmployeeDAO.new {
                name = "john doe"
                empid = "john.doe"
                email = "johndoe@acme.io"
                jobtitle = "developer"
                phone = "0123456789"
                department = deptTest
            }
        }
    }

    private fun hikariConfiguration(driverClassName: String, jdbcURL: String, userName : String, passW : String): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = driverClassName
        config.jdbcUrl = jdbcURL
        config.username = userName
        config.password = passW
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: () -> T): T =
            withContext(Dispatchers.IO) {
                transaction { block() }
            }
}