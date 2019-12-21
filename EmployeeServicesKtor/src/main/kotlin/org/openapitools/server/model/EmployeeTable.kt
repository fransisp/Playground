package org.openapitools.server.model

import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.select
import org.openapitools.server.dao.Employee
import org.openapitools.server.mapper.employeeRowGatewayToEmployeeDAO
import org.openapitools.server.utils.DatabaseFactory

/**
 * Table data gateway object for the Employee table
 */
object Employees : LongIdTable() {
    val name = varchar("name", 50)
    val empid = varchar("empid", 50).uniqueIndex()
    val email = varchar("email", 50)
    val phone = varchar("phone", 50)
    val jobtitle = varchar("jobtitle", 50)
    val department = reference("department_id", Departments)
}
/**
 * Find Employee info for given employee name
 */
suspend fun getEmployeeBasedOnName (employeeName: String) : Iterable<Employee> = DatabaseFactory.dbQuery {
    Employees.select { Employees.name like employeeName }.map { employeeRowGatewayToEmployeeDAO(it) }
}