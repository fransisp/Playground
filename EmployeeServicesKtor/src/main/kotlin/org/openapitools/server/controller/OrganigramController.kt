package org.openapitools.server.controller

import org.openapitools.server.dao.BranchDao
import org.openapitools.server.dao.DepartmentDAO
import org.openapitools.server.models.Branch
import org.openapitools.server.models.Department
import org.openapitools.server.models.Organigram
import org.openapitools.server.utils.DatabaseFactory

suspend fun getDepartmentInfo(branchID: Long, departmentID: Long): Organigram? = DatabaseFactory.dbQuery {
    BranchDao.findById(branchID)?.departments?.first { it.id.value == departmentID }.let { deptDAOToOrganigramModel(it) }
}

fun deptDAOToOrganigramModel(deptDAO: DepartmentDAO?): Organigram? {
    return when (deptDAO == null) {
        true -> null
        false -> Department(id = deptDAO.id.value,
                name = deptDAO.name,
                lead = deptDAO.lead,
                description = deptDAO.description
        )
    }
}

suspend fun getBranchInfo(branchID: Long): Organigram? = DatabaseFactory.dbQuery {
    BranchDao.findById(branchID).let { branchDAOToOrganigramModel(it) }
}

fun branchDAOToOrganigramModel(branchDao: BranchDao?): Organigram? {
    return when (branchDao == null) {
        true -> null
        false -> Branch(id = branchDao.id.value,
                name = branchDao.name,
                lead = branchDao.lead,
                description = branchDao.description,
                location = branchDao.location
        )
    }
}