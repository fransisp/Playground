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
import com.google.gson.GsonBuilder
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
import org.openapitools.server.controller.getEmployeeBasedOnName
import org.openapitools.server.infrastructure.Paths

@KtorExperimentalLocationsAPI
fun Route.employeeApi() {
    /**
     * A Gson Builder with pretty printing enabled.
     */
    val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    authenticate {
        get<Paths.GetMemberInfo> {
            val principal = call.authentication.principal<UserIdPrincipal>()?.name
            val inputQuery = call.parameters["employeeID"] ?: ""

            if (principal == null) {
                call.respond(HttpStatusCode.Unauthorized)
            } else {
                val exampleContentType = "application/json"
                val exampleContent = getEmployeeBasedOnName(inputQuery)

                if (exampleContent.count() == 0) call.response.status(HttpStatusCode.NotFound)
                else {
                    when (exampleContentType) {
                        "application/xml" -> call.respondText(gson.toJson(exampleContent.elementAt(0)), ContentType.Text.Xml)
                        else -> call.respond(exampleContent.elementAt(0).toString())
                    }
                }
            }
        }
    }
}
