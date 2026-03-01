package org.delcom.repositories

import org.delcom.dao.SportDAO
import org.delcom.entities.Sport
import org.delcom.helpers.daoToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.SportTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import java.util.UUID

class SportRepository : ISportRepository {

    override suspend fun getSports(search: String): List<Sport> = suspendTransaction {
        if (search.isBlank()) {
            SportDAO.all()
                .orderBy(SportTable.createdAt to SortOrder.DESC)
                .limit(20)
                .map(::daoToModel)
        } else {
            val keyword = "%${search.lowercase()}%"

            SportDAO
                .find {
                    SportTable.nama.lowerCase() like keyword
                }
                .orderBy(SportTable.nama to SortOrder.ASC)
                .limit(20)
                .map(::daoToModel)
        }
    }

    override suspend fun getSportById(id: String): Sport? = suspendTransaction {
        SportDAO
            .find { SportTable.id eq UUID.fromString(id) }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun getSportByName(name: String): Sport? = suspendTransaction {
        SportDAO
            .find { SportTable.nama eq name }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun addSport(sport: Sport): String = suspendTransaction {
        val sportDAO = SportDAO.new {
            nama = sport.nama
            pathGambar = sport.pathGambar
            deskripsi = sport.deskripsi
            kelebihan = sport.kelebihan
            teknologi = sport.teknologi
            tenaga = sport.tenaga
            createdAt = sport.createdAt
            updatedAt = sport.updatedAt
        }

        sportDAO.id.value.toString()
    }

    override suspend fun updateSport(id: String, newSport: Sport): Boolean = suspendTransaction {
        val sportDAO = SportDAO
            .find { SportTable.id eq UUID.fromString(id) }
            .limit(1)
            .firstOrNull()

        if (sportDAO != null) {
            sportDAO.nama = newSport.nama
            sportDAO.pathGambar = newSport.pathGambar
            sportDAO.deskripsi = newSport.deskripsi
            sportDAO.kelebihan = newSport.kelebihan
            sportDAO.teknologi = newSport.teknologi
            sportDAO.tenaga = newSport.tenaga
            sportDAO.updatedAt = newSport.updatedAt
            true
        } else {
            false
        }
    }

    override suspend fun removeSport(id: String): Boolean = suspendTransaction {
        val rowsDeleted = SportTable.deleteWhere {
            SportTable.id eq UUID.fromString(id)
        }
        rowsDeleted == 1
    }
}