package org.openapitools.server.controller

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.openapitools.server.dao.EmployeeDAO
import org.openapitools.server.dao.Employees
import org.openapitools.server.models.Employee

fun getEmployeeInfo (employeeName:String) : Iterable<EmployeeDAO>
{
    return EmployeeDAO.find { Op.build { Employees.name.like(employeeName) } }
}
