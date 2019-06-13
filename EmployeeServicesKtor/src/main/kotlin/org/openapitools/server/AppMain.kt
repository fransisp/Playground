package org.openapitools.server

import com.codahale.metrics.Slf4jReporter
import com.typesafe.config.ConfigFactory
import io.ktor.application.Application
import io.ktor.application.ApplicationStopping
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.auth.Authentication
import io.ktor.auth.UserHashedTableAuth
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.basic
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.config.HoconApplicationConfig
import io.ktor.features.*
import io.ktor.gson.GsonConverter
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.metrics.dropwizard.DropwizardMetrics
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getDigestFunction
import org.jetbrains.exposed.sql.Database
import org.openapitools.server.apis.OrganigramApi
import org.openapitools.server.apis.employeeApi
import org.openapitools.server.infrastructure.ApiKeyCredential
import org.openapitools.server.infrastructure.ApiPrincipal
import org.openapitools.server.infrastructure.apiKeyAuth
import org.openapitools.server.service.DatabaseFactory
import java.util.*
import java.util.concurrent.TimeUnit


@KtorExperimentalAPI
internal val settings = HoconApplicationConfig(ConfigFactory.defaultApplication(HTTP::class.java.classLoader))

object HTTP {
    val client = HttpClient(Apache)
}

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Application.main() {
    DatabaseFactory.init("org.h2.Driver", jdbcURL = settings.property("database.jdbcUrl").getString(),
            username = settings.property("database.dbUser").getString(), password = settings.property("database.dbPassword").getString())
    install(DefaultHeaders)
    install(DropwizardMetrics) {
        val reporter = Slf4jReporter.forRegistry(registry)
                .outputTo(log)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build()
        reporter.start(10, TimeUnit.SECONDS)
    }
    install(ContentNegotiation) {
        register(ContentType.Application.Json, GsonConverter())
        gson {
            setPrettyPrinting()
        }
    }
    install(CORS) {
        anyHost()
        allowCredentials = true
        listOf(HttpMethod("PATCH"), HttpMethod.Put, HttpMethod.Delete).forEach {
            method(it)
        }
    }// Enables Cross-Origin Resource Sharing (CORS)
    install(AutoHeadResponse) // see http://ktor.io/features/autoheadresponse.html
    install(HSTS, ApplicationHstsConfiguration()) // see http://ktor.io/features/hsts.html
    install(Compression, ApplicationCompressionConfiguration()) // see http://ktor.io/features/compression.html
    install(Locations) // see http://ktor.io/features/locations.html
    install(Authentication) {
        // "Implement API key auth (api_key) for parameter name 'X-API-KEY'."
        /*apiKeyAuth("api_key") {
            validate { apikeyCredential: ApiKeyCredential ->
                when {
                    apikeyCredential.value == "keyboardcat" -> ApiPrincipal(apikeyCredential)
                    else -> null
                }
            }
        }*/
        basic {
            realm = "myrealm"
            validate { credentials ->
                if (credentials.name == credentials.password) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
    install(Routing) {
        employeeApi()
        OrganigramApi()
    }


    environment.monitor.subscribe(ApplicationStopping)
    {
        HTTP.client.close()
    }
}
