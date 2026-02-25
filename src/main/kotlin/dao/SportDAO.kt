package org.delcom.dao

import org.delcom.tables.SportTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID


class SportDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, SportDAO>(SportTable)

    var nama by SportTable.nama
    var pathGambar by SportTable.pathGambar
    var deskripsi by SportTable.deskripsi
    var kelebihan by SportTable.kelebihan
    var teknologi_yang_digunkan by SportTable.teknologi_yang_digunkan
    var Tenaga by SportTable.Tenaga
    var createdAt by SportTable.createdAt
    var updatedAt by SportTable.updatedAt
}