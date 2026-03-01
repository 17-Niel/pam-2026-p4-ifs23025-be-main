package org.delcom.data

import kotlinx.serialization.Serializable
import org.delcom.entities.Sport

@Serializable
data class SportRequest(
    var nama: String = "",
    var deskripsi: String = "",
    var kelebihan: String = "",
    var teknologi: String = "",
    var tenaga: String = "",
    var pathGambar: String = "",
){
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "nama" to nama,
            "deskripsi" to deskripsi,
            "kelebihan" to kelebihan,
            "teknologi" to teknologi,
            "tenaga" to tenaga,
            "pathGambar" to pathGambar
        )
    }

    fun toEntity(): Sport {
        return Sport(
            nama = nama,
            deskripsi = deskripsi,
            kelebihan = kelebihan,
            teknologi = teknologi,
            tenaga = tenaga,
            pathGambar =  pathGambar,
        )
    }

}