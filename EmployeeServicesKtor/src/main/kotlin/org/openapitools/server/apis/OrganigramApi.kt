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
package org.openapitools.server.apis


import com.google.gson.Gson
import io.ktor.application.call
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import org.openapitools.server.infrastructure.Paths
import org.openapitools.server.controller.getBranchInfo
import org.openapitools.server.controller.getDepartmentInfo

@KtorExperimentalLocationsAPI
fun Route.OrganigramApi() {
    val gson = Gson()

    authenticate {
        get<Paths.getBranchInfo> {
            val principal = call.authentication.principal<UserIdPrincipal>()?.name
            val inputBranchID = call.parameters["branchID"]?.toLong() ?: -1L

            if (principal == null) {
                call.respond(HttpStatusCode.Unauthorized)
            } else {
                val exampleContentType = "application/json"
                val exampleContent = getBranchInfo(branchID = inputBranchID)

                if (exampleContent == null) call.response.status(HttpStatusCode.NotFound)
                else {
                    when (exampleContentType) {
                        "application/xml" -> call.respondText(gson.toJson(exampleContent), ContentType.Text.Xml)
                        else -> call.respond(gson.toJson(exampleContent))
                    }
                }
            }
        }


        get<Paths.getDepartmentInfo> {
            val principal = call.authentication.principal<UserIdPrincipal>()?.name
            val inputDeptID = call.parameters["departmentID"]?.toLong() ?: -1L
            val inputBranchID = call.parameters["branchID"]?.toLong() ?: -1L

            if (principal == null) {
                call.respond(HttpStatusCode.Unauthorized)
            } else {
                val exampleContentType = "application/json"
                val exampleContent = getDepartmentInfo(branchID = inputBranchID, departmentID = inputDeptID)

                if (exampleContent == null) call.response.status(HttpStatusCode.NotFound)
                else {
                    when (exampleContentType) {
                        "application/xml" -> call.respondText(gson.toJson(exampleContent), ContentType.Text.Xml)
                        else -> call.respond(gson.toJson(exampleContent))
                    }
                }
            }
        }
    }
}
