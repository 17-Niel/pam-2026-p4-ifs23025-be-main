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
                Pair("tentang", "I am a highly skilled and dedicated software developer with strong expertise in Android, iOS, and web application development. I specialize in building responsive, scalable, and high-performance applications with a strong focus on clean architecture, security, and user experience. With solid experience working both independently and within cross-functional teams, I am able to translate business requirements into innovative and efficient digital solutions. I am committed to delivering high-quality products that meet industry standards and provide exceptional value to users."),
            )
        )
        call.respond(response)
    }

    // Mengambil photo profile
    suspend fun getProfilePhoto(call: ApplicationCall) {
        val file = File("uploads/profile/me.jpg")

        if (!file.exists()) {
            return call.respond(HttpStatusCode.NotFound)
        }

        call.respondFile(file)
    }
}