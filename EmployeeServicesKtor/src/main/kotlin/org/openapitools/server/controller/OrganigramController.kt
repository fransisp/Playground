package org.openapitools.server.controller

import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.openapitools.server.dao.Branches
import org.openapitools.server.dao.Departments
import org.openapitools.server.models.Branch
import org.openapitools.server.models.Department
import org.openapitools.server.models.Organigram
import org.openapitools.server.utils.DatabaseFactory

suspend fun getDepartmentInfo(branchID: Long, departmentID: Long): Iterable<Organigram> = DatabaseFactory.dbQuery {
    Departments.select { (Departments.id eq departmentID) and (Departments.branch eq branchID) }.let { deptDAOToOrganigramModel(it) }
}

fun deptDAOToOrganigramModel(deptQuery: Query): Iterable<Organigram> {

    return when (deptQuery.fetchSize == 0) {
        true -> emptyList()
        false -> {
            deptQuery.flatMap {
                listOf(Department(id = it[Departments.id].value,
                        name = it[Departments.name],
                        lead = it[Departments.lead],
                        description = it[Departments.description]))
            }
        }
    }
}

suspend fun getBranchInfo(branchID: Long): Iterable<Organigram> = DatabaseFactory.dbQuery {
    Branches.select { Branches.id eq branchID }.let { branchDAOToOrganigramModel(it) }
}

fun branchDAOToOrganigramModel(branchQuery: Query): Iterable<Organigram> {
    return when (branchQuery.fetchSize == 0) {
        true -> emptyList()
        false -> {
            branchQuery.flatMap {
                listOf(Branch(id = it[Branches.id].value,
                        name = it[Branches.name],
                        lead = it[Branches.lead],
                        description = it[Branches.description],
                        location = it[Branches.location]))
            }
        }
    }
}