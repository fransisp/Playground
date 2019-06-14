package org.openapitools.server.controller

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.openapitools.server.dao.Employees
import org.openapitools.server.models.Employee
import org.openapitools.server.utils.DatabaseFactory.dbQuery

suspend fun getEmployeeBasedOnName (employeeName:String) : Iterable<Employee> = dbQuery {
    Employees.select{ Employees.name like employeeName }.map { toEmployeeModel(it) }
}

private fun toEmployeeModel (employeeRow:ResultRow) : Employee = Employee(id = employeeRow[Employees.id].value,
            name = employeeRow[Employees.name],
            empid =  employeeRow[Employees.empid],
            email = employeeRow[Employees.email],
            phone =  employeeRow[Employees.phone],
            jobtitle = employeeRow[Employees.jobtitle]
            )