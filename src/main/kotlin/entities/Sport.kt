package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Sport(
    var id : String = UUID.randomUUID().toString(),
    var nama: String,
    var pathGambar: String,
    var deskripsi: String,
    val kelebihan: String,
    val teknologi: String,
    val tenaga: String,

    @Contextual
    val createdAt: Instant = Clock.System.now(),
    @Contextual
    var updatedAt: Instant = Clock.System.now(),
) {
    // URL LENGKAP untuk gambar (bisa langsung dipakai frontend)
    val gambar: String
        get() {
            return if (pathGambar.isNotEmpty()) {
                val filename = pathGambar.substringAfterLast("/")
                "https://pam-2026-p4-ifs23025-be.nieltobing.fun:8080/static/sports/$filename"
            } else {
                ""
            }
        }
}