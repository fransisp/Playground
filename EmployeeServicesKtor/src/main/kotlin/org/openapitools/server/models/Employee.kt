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
package org.openapitools.server.models


/**
 *
 * @param id
 * @param name
 * @param empid
 * @param email
 * @param phone
 * @param jobtitle
 */
data class Employee(
        val id: Long? = null,
        val name: String? = null,
        val empid: String? = null,
        val email: String? = null,
        val phone: String? = null,
        val jobtitle: String? = null
) {

}
