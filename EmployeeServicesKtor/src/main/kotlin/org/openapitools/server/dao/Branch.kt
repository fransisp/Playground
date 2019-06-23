/**
 * Organisation Services
 * These are the services for the organisation site. These will be called by the frontend to provide the data
 *
 * OpenAPI spec version: 1.0.0
 * Contact: fr.prayuda@gmail.com
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package org.openapitools.server.dao

/**
 * DAO for Branch
 * @param id id of the organisation branch
 * @param name branch name
 * @param lead lead of the organisation branch
 * @param description short explanation of the organisation
 * @param location city where the organisation is located
 */
data class Branch(
        override val id: Long,
        override val name: String,
        override val lead: String,
        override val description: String,
        val location: String
) : Organigram