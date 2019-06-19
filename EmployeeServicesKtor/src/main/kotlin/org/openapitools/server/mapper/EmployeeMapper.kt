package org.openapitools.server.mapper

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.openapitools.server.model.Employees
import org.openapitools.server.dao.Employee
import org.openapitools.server.utils.DatabaseFactory.dbQuery

/**
 * Function to map Employee Row Data Gateway object to the Employee DAO model
 */
fun employeeRowGatewayToEmployeeDAO (employeeRow:ResultRow) : Employee = Employee(id = employeeRow[Employees.id].value,
            name = employeeRow[Employees.name],
            empid =  employeeRow[Employees.empid],
            email = employeeRow[Employees.email],
            phone =  employeeRow[Employees.phone],
            jobtitle = employeeRow[Employees.jobtitle]
            )