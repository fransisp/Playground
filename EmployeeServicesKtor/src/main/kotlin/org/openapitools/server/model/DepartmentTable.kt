package org.openapitools.server.model

import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.openapitools.server.dao.Organigram
import org.openapitools.server.mapper.deptRowGatewayToOrganigramDAO
import org.openapitools.server.utils.DatabaseFactory

/**
 * Table data gateway object for the Department table
 */
object Departments : LongIdTable() {
    val name = varchar("name", 50)
    val lead = varchar("lead", 50)
    val description = varchar("description", 128)
    val branch = reference("department", Branches)
}

/**
 * Find Department info for given Branch and Department ID
 */
suspend fun getDepartmentInfo(branchID: Long, departmentID: Long): Iterable<Organigram> = DatabaseFactory.dbQuery {
    Departments.select { (Departments.id eq departmentID) and (Departments.branch eq branchID) }.let { deptRowGatewayToOrganigramDAO(it) }
}