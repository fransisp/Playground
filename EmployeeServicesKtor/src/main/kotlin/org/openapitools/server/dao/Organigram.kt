package org.openapitools.server.dao

/**
 * Interface to be used as template for Organigram objects (Branches & Department)
 */
interface Organigram {
    val id: Long
    val name: String
    val lead: String
    val description: String
}