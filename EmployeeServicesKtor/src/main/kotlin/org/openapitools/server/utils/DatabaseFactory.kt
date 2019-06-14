package org.openapitools.server.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
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

            Employees.insert {
                it[name] = "john doe"
                it[empid] = "john.doe"
                it[email] = "johndoe@acme.io"
                it[jobtitle] = "developer"
                it[phone] = "0123456789"
                it[department] = deptTest.id
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