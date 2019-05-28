package org.openapitools.server.controller

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.openapitools.server.dao.EmployeeDAO
import org.openapitools.server.dao.Employees
import org.openapitools.server.models.Employee
import org.openapitools.server.service.DatabaseFactory.dbQuery

suspend fun getEmployeeInfo (employeeName:String) : Iterable<Employee> = dbQuery {
        EmployeeDAO.find { Op.build { Employees.name.like(employeeName) } }.map { toEmployeeModel(it) }
    }

fun toEmployeeModel (employeeDAO:EmployeeDAO) : Employee
{
    return Employee(id = employeeDAO.id.value,
            name = employeeDAO.name,
            empid =  employeeDAO.empid,
            email = employeeDAO.email,
            phone =  employeeDAO.phone,
            jobtitle = employeeDAO.jobtitle
            )
}