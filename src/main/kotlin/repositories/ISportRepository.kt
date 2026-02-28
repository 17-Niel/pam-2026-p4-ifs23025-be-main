package org.delcom.repositories

import org.delcom.entities.Sport

interface  ISportRepository {
    suspend fun getSports(search: String): List<Sport>
    suspend fun getSportById(id: String): Sport?
    suspend fun getSportByName(name: String): Sport?
    suspend fun addSport(sport: Sport) : String
    suspend fun updateSport(id: String, newSport: Sport): Boolean
    suspend fun removeSport(id: String): Boolean
}