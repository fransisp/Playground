package org.openapitools.server.dao

import org.jetbrains.exposed.dao.*

object Branches : LongIdTable() {
    val name = varchar("name", 50)
    val lead = varchar("lead", 50)
    val description = varchar("description", 128)
}

class BranchDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<BranchDao>(Branches)

    var name by Branches.name
    var lead by Branches.lead
    var description by Branches.description
    val departments by DepartmentDAO referrersOn Departments.branch
}