package org.openapitools.server.model

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.sql.select
import org.openapitools.server.dao.Organigram
import org.openapitools.server.mapper.branchRowGatewayToOrganigramDAO
import org.openapitools.server.utils.DatabaseFactory

/**
 * Table data gateway object for the Branch table
 */
object Branches : LongIdTable() {
    val name = varchar("name", 50)
    val lead = varchar("lead", 50)
    val description = varchar("description", 128)
    val location = varchar("location", 50)
}

/**
 * Find Branch info for given ID
 */
suspend fun getBranchInfo(branchID: Long): Iterable<Organigram> = DatabaseFactory.dbQuery {
    Branches.select { Branches.id eq branchID }.let { branchRowGatewayToOrganigramDAO(it) }
}