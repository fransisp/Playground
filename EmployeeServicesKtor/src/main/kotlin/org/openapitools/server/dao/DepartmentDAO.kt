package org.openapitools.server.dao

import org.jetbrains.exposed.dao.*

object Departments : LongIdTable() {
    val name = varchar("name", 50)
    val lead = varchar("lead", 50)
    val description = varchar("description", 128)
    val branch = reference("department", Branches)
}

class DepartmentDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DepartmentDAO>(Departments)

    var name by Departments.name
    var lead by Departments.lead
    var description by Departments.description
    //val employees by EmployeeDAO referrersOn Employees.department
    var branch by BranchDao referencedOn Departments.branch
}