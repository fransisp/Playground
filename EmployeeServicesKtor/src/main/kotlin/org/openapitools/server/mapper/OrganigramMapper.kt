package org.openapitools.server.mapper

import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.openapitools.server.model.Branches
import org.openapitools.server.model.Departments
import org.openapitools.server.dao.Branch
import org.openapitools.server.dao.Department
import org.openapitools.server.dao.Organigram
import org.openapitools.server.utils.DatabaseFactory

/**
 * Function to map Department Row Data Gateway object to the Department DAO model
 */
fun deptRowGatewayToOrganigramDAO(deptQuery: Query): Iterable<Organigram> {

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

/**
 * Function to map Branch Row Data Gateway object to the Branch DAO model
 */
fun branchRowGatewayToOrganigramDAO(branchQuery: Query): Iterable<Organigram> {
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