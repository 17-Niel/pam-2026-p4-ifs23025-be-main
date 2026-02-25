package org.delcom.repositories

import org.delcom.entities.Sport

interface  IPlantRepository {
    suspend fun getPlants(search: String): List<Sport>
    suspend fun getPlantById(id: String): Sport?
    suspend fun getPlantByName(name: String): Sport?
    suspend fun addPlant(plant: Sport) : String
    suspend fun updatePlant(id: String, newPlant: Sport): Boolean
    suspend fun removePlant(id: String): Boolean
}