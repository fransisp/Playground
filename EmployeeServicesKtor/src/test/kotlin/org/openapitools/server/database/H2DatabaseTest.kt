package org.openapitools.server.database

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.openapitools.server.dao.EmployeeDAO
import org.openapitools.server.dao.Employees
import org.openapitools.server.dao.Employees.empid
import org.openapitools.server.models.Employee

object Users : IntIdTable() {
    val name = varchar("name", 50).index()
    val city = reference("city", Cities)
    val age = integer("age")
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var name by Users.name
    var city by City referencedOn Users.city
    var age by Users.age
}

object Cities : IntIdTable() {
    val name = varchar("name", 50)
}

class City(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<City>(Cities)

    var name by Cities.name
    val users by User referrersOn Users.city
}

class ExposedDAOTest {
    @Test
    fun `can do simple h2 database ops`() {
        Database.connect("jdbc:h2:mem:test;IGNORECASE=TRUE", driver = "org.h2.Driver")

        transaction {
            addLogger(StdOutSqlLogger)

            SchemaUtils.create(Cities, Users, Employees)

            val stPete = City.new {
                name = "St. Petersburg"
            }

            val munich = City.new {
                name = "Munich"
            }

            User.new {
                name = "a"
                city = stPete
                age = 5
            }

            User.new {
                name = "b"
                city = stPete
                age = 27
            }

            User.new {
                name = "c"
                city = munich
                age = 42
            }


            val empTest = EmployeeDAO.new {
                name = "john doe"
                empid = "john.doe"
                email = "johndoe@acme.io"
                jobtitle = "developer"
                phone = "0123456789"
            }

            EmployeeDAO.find(Op.build { empid.eq("john.doe") })
                    .forEach{
                        assertThat(it == empTest)
                    }

            println("Cities: ${City.all().joinToString { it.name }}")
            println("Users in ${stPete.name}: ${stPete.users.joinToString { it.name }}")
            println("Adults: ${User.find { Users.age greaterEq 18 }.joinToString { it.name }}")
        }
    }
}