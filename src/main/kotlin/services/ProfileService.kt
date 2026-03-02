package org.delcom.services

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.delcom.data.DataResponse
import java.io.File

class ProfileService {

    suspend fun getProfile(call: ApplicationCall) {
        // Mock data yang lebih mendalam
        val profileData = mapOf(
            "header" to mapOf(
                "username" to "niel.valvoline",
                "full_name" to "Daniel L. Tobing",
                "headline" to "Senior Mobile Engineer | Android & KMM Specialist",
                "avatar_url" to "/profile/photo",
                "location" to "Jakarta, Indonesia"
            ),
            "stats" to mapOf(
                "followers" to 1250,
                "following" to 350,
                "projects_completed" to 48,
                "years_exp" to 5
            ),
            "bio" to "A high-performance developer specializing in building scalable mobile architectures and sleek user interfaces. Passionate about merging automotive aesthetics with cutting-edge technology. 🚀",
            "tech_stack" to listOf(
                mapOf("name" to "Kotlin", "level" to "Expert", "icon" to "kotlin_icon"),
                mapOf("name" to "Jetpack Compose", "level" to "Expert", "icon" to "compose_icon"),
                mapOf("name" to "KMM", "level" to "Advanced", "icon" to "kmm_icon"),
                mapOf("name" to "Coroutines", "level" to "Expert", "icon" to "async_icon")
            ),
            "links" to mapOf(
                "github" to "https://github.com/daniell.tobing",
                "portfolio" to "https://daniel-portfolio.vercel.app",
                "linkedin" to "https://linkedin.com/in/danieltobing"
            )
        )

        val response = DataResponse(
            status = "success",
            message = "Profile data fetched successfully",
            data = profileData
        )

        call.respond(HttpStatusCode.OK, response)
    }

    suspend fun getProfilePhoto(call: ApplicationCall) {
        val file = File("uploads/profile/me.jpg")
        if (file.exists()) {
            call.respondFile(file)
        } else {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Image not found"))
        }
    }
}