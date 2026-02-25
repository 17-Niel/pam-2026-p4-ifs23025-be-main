package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object SportTable : UUIDTable("plants") {
    val nama = varchar("nama", 100)
    val pathGambar = varchar("path_gambar", 255)
    val deskripsi = text("deskripsi")
    val kelebihan = text("kelebihan")
    val teknologi_yang_digunkan = text("teknologi_yang_digunkan")
    val Tenaga = text("Tenaga")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}