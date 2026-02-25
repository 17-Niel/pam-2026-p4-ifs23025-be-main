package org.delcom.data

import kotlinx.serialization.Serializable
import org.delcom.entities.Sport

@Serializable
data class SportRequest(
    var nama: String = "",
    var deskripsi: String = "",
    var kelebihan: String = "",
    var teknologi_yang_digunkan: String = "",
    var Tenaga: String = "",
    var pathGambar: String = "",
){
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "nama" to nama,
            "deskripsi" to deskripsi,
            "kelebihan" to kelebihan,
            "teknologi_yang_digunkan" to teknologi_yang_digunkan,
            "Tenaga" to Tenaga,
            "pathGambar" to pathGambar
        )
    }

    fun toEntity(): Sport {
        return Sport(
            nama = nama,
            deskripsi = deskripsi,
            kelebihan = kelebihan,
            teknologi_yang_digunkan = teknologi_yang_digunkan,
            Tenaga = Tenaga,
            pathGambar =  pathGambar,
        )
    }

}