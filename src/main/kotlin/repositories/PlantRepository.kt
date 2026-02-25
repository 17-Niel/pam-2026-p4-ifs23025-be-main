package org.delcom.repositories

import org.delcom.dao.SportDAO
import org.delcom.entities.Sport
import org.delcom.helpers.daoToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.SportTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import java.util.UUID

class PlantRepository : IPlantRepository {
    override suspend fun getPlants(search: String): List<Sport> = suspendTransaction {
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

    override suspend fun getPlantById(id: String): Sport? = suspendTransaction {
        SportDAO
            .find { (SportTable.id eq UUID.fromString(id)) }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun getPlantByName(name: String): Sport? = suspendTransaction {
        SportDAO
            .find { (SportTable.nama eq name) }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun addPlant(plant: Sport): String = suspendTransaction {
        val plantDAO = SportDAO.new {
            nama = plant.nama
            pathGambar = plant.pathGambar
            deskripsi = plant.deskripsi
            kelebihan = plant.kelebihan
            teknologi_yang_digunkan = plant.teknologi_yang_digunkan
            Tenaga = plant.Tenaga
            createdAt = plant.createdAt
            updatedAt = plant.updatedAt
        }

        plantDAO.id.value.toString()
    }

    override suspend fun updatePlant(id: String, newPlant: Sport): Boolean = suspendTransaction {
        val plantDAO = SportDAO
            .find { SportTable.id eq UUID.fromString(id) }
            .limit(1)
            .firstOrNull()

        if (plantDAO != null) {
            plantDAO.nama = newPlant.nama
            plantDAO.pathGambar = newPlant.pathGambar
            plantDAO.deskripsi = newPlant.deskripsi
            plantDAO.kelebihan = newPlant.kelebihan
            plantDAO.teknologi_yang_digunkan = newPlant.teknologi_yang_digunkan
            plantDAO.Tenaga = newPlant.Tenaga
            plantDAO.updatedAt = newPlant.updatedAt
            true
        } else {
            false
        }
    }

    override suspend fun removePlant(id: String): Boolean = suspendTransaction {
        val rowsDeleted = SportTable.deleteWhere {
            SportTable.id eq UUID.fromString(id)
        }
        rowsDeleted == 1
    }

}