package org.delcom

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureStaticContent() {
    routing {
        // Endpoint untuk mengakses gambar plants
        get("/static/plants/{filename}") {
            val filename = call.parameters["filename"]
            if (filename != null) {
                val file = File("uploads/plants/$filename")
                if (file.exists()) {
                    val contentType = when (file.extension.lowercase()) {
                        "png" -> ContentType.Image.PNG
                        "jpg", "jpeg" -> ContentType.Image.JPEG
                        "gif" -> ContentType.Image.GIF
                        else -> ContentType.Image.Any
                    }
                    call.response.header(HttpHeaders.ContentType, contentType.toString())
                    call.respondFile(file)
                } else {
                    call.respond(HttpStatusCode.NotFound, "File not found")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Filename required")
            }
        }

        // Endpoint untuk mengakses gambar sports
        get("/static/sports/{filename}") {
            val filename = call.parameters["filename"]
            if (filename != null) {
                val file = File("uploads/sports/$filename")
                if (file.exists()) {
                    val contentType = when (file.extension.lowercase()) {
                        "png" -> ContentType.Image.PNG
                        "jpg", "jpeg" -> ContentType.Image.JPEG
                        "gif" -> ContentType.Image.GIF
                        else -> ContentType.Image.Any
                    }
                    call.response.header(HttpHeaders.ContentType, contentType.toString())
                    call.respondFile(file)
                } else {
                    call.respond(HttpStatusCode.NotFound, "File not found")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Filename required")
            }
        }

        // Endpoint untuk mengakses gambar profile
        get("/static/profile/{filename}") {
            val filename = call.parameters["filename"]
            if (filename != null) {
                val file = File("uploads/profile/$filename")
                if (file.exists()) {
                    val contentType = when (file.extension.lowercase()) {
                        "png" -> ContentType.Image.PNG
                        "jpg", "jpeg" -> ContentType.Image.JPEG
                        "gif" -> ContentType.Image.GIF
                        else -> ContentType.Image.Any
                    }
                    call.response.header(HttpHeaders.ContentType, contentType.toString())
                    call.respondFile(file)
                } else {
                    call.respond(HttpStatusCode.NotFound, "File not found")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Filename required")
            }
        }
    }
}