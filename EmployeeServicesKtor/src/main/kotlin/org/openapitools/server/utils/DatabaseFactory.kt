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
import org.openapitools.server.model.Branches
import org.openapitools.server.model.Departments
import org.openapitools.server.model.Employees

/**
 * Factory object to initialized databes connection
 */
object DatabaseFactory {

    fun init(driverName:String, jdbcURL:String, username:String, password:String) {
        Database.connect(hikariConfiguration(driverName, jdbcURL, username, password))
        transaction {
            //create database schema and fill it in with dummy data
            SchemaUtils.create(Employees, Departments, Branches)
            val branchTest = Branches.insertAndGetId {
                it[name] = "Aperture"
                it[lead] = "GLaDOS"
                it[description] = "is anybody safe?"
                it[location] = "unknown"
            }

            val deptTest = Departments.insertAndGetId {
                it[name] = "Research"
                it[lead] = "mr. nobody"
                it[description] = "cool things happens here"
                it[branch] = branchTest
            }

            Employees.insert {
                it[name] = "john doe"
                it[empid] = "john.doe"
                it[email] = "johndoe@acme.io"
                it[jobtitle] = "developer"
                it[phone] = "0123456789"
                it[department] = deptTest
            }
        }
    }

    //configure hikari connection pool to manage all connections to the database
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

    //functions that receive lambda object consisting method need to be executed in the databse
    suspend fun <T> dbQuery(block: () -> T): T =
            withContext(Dispatchers.IO) {
                transaction { block() }
            }
}