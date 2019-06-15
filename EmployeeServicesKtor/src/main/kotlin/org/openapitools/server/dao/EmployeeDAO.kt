package org.openapitools.server.dao

import org.jetbrains.exposed.dao.*

object Employees : LongIdTable() {
    val name = varchar("name", 50)
    val empid = varchar("empid", 50).uniqueIndex()
    val email = varchar("email", 50)
    val phone = varchar("phone", 50)
    val jobtitle = varchar("jobtitle", 50)
    val department = reference("department_id", Departments)
}

/*class EmployeeDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<EmployeeDAO>(Employees)

    var name by Employees.name
    var empid by Employees.empid
    var email by Employees.email
    var phone by Employees.phone
    var jobtitle by Employees.jobtitle
    var department by DepartmentDAO referencedOn Employees.department
}*/