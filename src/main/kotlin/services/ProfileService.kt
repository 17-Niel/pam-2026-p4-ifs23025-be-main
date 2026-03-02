package org.delcom.services

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.delcom.data.DataResponse
import java.io.File

class ProfileService {
    // Mengambil semua data tumbuhan
    suspend fun getProfile(call: ApplicationCall) {
        val response = DataResponse(
            "success",
            "Berhasil mengambil profile pengembang",
            mapOf(
                Pair("username", "niel.valvoline"),
                Pair("nama", "Daniel L. Tobing"),
                Pair("tentang", "A high-performance developer specializing in building scalable mobile architectures and sleek user interfaces. Passionate about merging automotive aesthetics with cutting-edge technology."),
                // TAMBAHAN 1: Role/Job Title dengan style keren
                Pair("role", "🚀 Lead Mobile Developer | Automotive Enthusiast"),
                // TAMBAHAN 2: Stats sederhana
                Pair("stats", mapOf(
                    "projects" to 27,
                    "experience" to "5+ years",
                    "rating" to 4.8
                ))
            )
        )
        call.respond(response)
    }

    // Mengambil photo profile
    suspend fun getProfilePhoto(call: ApplicationCall) {
        val file = File("uploads/profile/me.png")

        if (!file.exists()) {
            return call.respond(HttpStatusCode.NotFound)
        }

        call.respondFile(file)
    }
}