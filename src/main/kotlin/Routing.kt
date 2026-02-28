package org.delcom

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.delcom.data.AppException
import org.delcom.data.ErrorResponse
import org.delcom.helpers.parseMessageToMap
import org.delcom.services.PlantService
import org.delcom.services.ProfileService
import org.delcom.services.SportService
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val plantService: PlantService by inject()
    val profileService: ProfileService by inject()
    val sportService: SportService by inject() // ✅ ditambahkan

    install(StatusPages) {
        exception<AppException> { call, cause ->
            val dataMap: Map<String, List<String>> = parseMessageToMap(cause.message)

            call.respond(
                status = HttpStatusCode.fromValue(cause.code),
                message = ErrorResponse(
                    status = "fail",
                    message = if (dataMap.isEmpty()) cause.message else "Data yang dikirimkan tidak valid!",
                    data = if (dataMap.isEmpty()) null else dataMap.toString()
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.fromValue(500),
                message = ErrorResponse(
                    status = "error",
                    message = cause.message ?: "Unknown error",
                    data = ""
                )
            )
        }
    }

    routing {
        get("/") {
            call.respondText("API telah berjalan. Dibuat oleh Daniel L. Tobing.")
        }

        // ======================
        // Route Plants (tidak diubah)
        // ======================
        route("/plants") {
            get {
                plantService.getAllPlants(call)
            }
            post {
                plantService.createPlant(call)
            }
            get("/{id}") {
                plantService.getPlantById(call)
            }
            put("/{id}") {
                plantService.updatePlant(call)
            }
            delete("/{id}") {
                plantService.deletePlant(call)
            }
            get("/{id}/image") {
                plantService.getPlantImage(call)
            }
        }

        // ======================
        // Route Sports (DITAMBAHKAN)
        // ======================
        route("/sports") {
            get {
                sportService.getAllSports(call)
            }
            post {
                sportService.createSport(call)
            }
            get("/{id}") {
                sportService.getSportById(call)
            }
            put("/{id}") {
                sportService.updateSport(call)
            }
            delete("/{id}") {
                sportService.deleteSport(call)
            }
            get("/{id}/image") {
                sportService.getSportImage(call)
            }
        }

        // Route Profile (tidak diubah)
        route("/profile") {
            get {
                profileService.getProfile(call)
            }
            get("/photo") {
                profileService.getProfilePhoto(call)
            }
        }
    }
}