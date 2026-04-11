package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.PlantRequest
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IPlantRepository
import java.io.File
import java.util.*

class PlantService(private val plantRepository: IPlantRepository) {

    suspend fun getAllPlants(call: ApplicationCall) {
        try {
            val search = call.request.queryParameters["search"] ?: ""
            println("📋 GET /plants - search: $search")

            val plants = plantRepository.getPlants(search)
            println("📋 Ditemukan ${plants.size} tanaman")

            val response = DataResponse(
                "success",
                "Berhasil mengambil daftar tumbuhan",
                mapOf("plants" to plants)
            )
            call.respond(response)
        } catch (e: Exception) {
            println("❌ Error getAllPlants: ${e.message}")
            throw e
        }
    }

    suspend fun getPlantById(call: ApplicationCall) {
        val id = call.parameters["id"]
        println("📋 GET /plants/$id")

        if (id == null) {
            throw AppException(400, "ID tumbuhan tidak boleh kosong!")
        }

        val plant = plantRepository.getPlantById(id)
        if (plant == null) {
            throw AppException(404, "Data tumbuhan tidak tersedia!")
        }

        val response = DataResponse(
            "success",
            "Berhasil mengambil data tumbuhan",
            mapOf("plant" to plant)
        )
        call.respond(response)
    }

    private suspend fun getPlantRequest(call: ApplicationCall): PlantRequest {
        val plantReq = PlantRequest()

        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 10)
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "nama" -> plantReq.nama = part.value.trim()
                        "deskripsi" -> plantReq.deskripsi = part.value
                        "manfaat" -> plantReq.manfaat = part.value
                        "efekSamping" -> plantReq.efekSamping = part.value
                    }
                    println("📝 Field ${part.name}: ${part.value.take(50)}")
                }

                is PartData.FileItem -> {
                    val originalName = part.originalFileName ?: "file"
                    val ext = originalName.substringAfterLast('.', "")
                    val fileName = UUID.randomUUID().toString() + if (ext.isNotEmpty()) ".$ext" else ""
                    val filePath = "uploads/plants/$fileName"

                    val file = File(filePath)
                    file.parentFile.mkdirs()

                    part.provider().copyAndClose(file.writeChannel())
                    plantReq.pathGambar = filePath
                    println("📸 Upload gambar: $filePath")
                }

                else -> {}
            }
            part.dispose()
        }

        return plantReq
    }

    private fun validatePlantRequest(plantReq: PlantRequest, isUpdate: Boolean = false) {
        val validatorHelper = ValidatorHelper(plantReq.toMap())
        validatorHelper.required("nama", "Nama tidak boleh kosong")
        validatorHelper.required("deskripsi", "Deskripsi tidak boleh kosong")
        validatorHelper.required("manfaat", "Manfaat tidak boleh kosong")
        validatorHelper.required("efekSamping", "Efek Samping tidak boleh kosong")

        if (!isUpdate || plantReq.pathGambar.isNotEmpty()) {
            validatorHelper.required("pathGambar", "Gambar tidak boleh kosong")
        }

        validatorHelper.validate()

        if (plantReq.pathGambar.isNotEmpty()) {
            val file = File(plantReq.pathGambar)
            if (!file.exists()) {
                throw AppException(400, "Gambar tumbuhan gagal diupload!")
            }
        }
    }

    suspend fun createPlant(call: ApplicationCall) {
        println("📝 POST /plants - Create new plant")
        val plantReq = getPlantRequest(call)

        validatePlantRequest(plantReq, isUpdate = false)

        val existPlant = plantRepository.getPlantByName(plantReq.nama)
        if (existPlant != null) {
            val tmpFile = File(plantReq.pathGambar)
            if (tmpFile.exists()) {
                tmpFile.delete()
            }
            throw AppException(409, "Tumbuhan dengan nama ini sudah terdaftar!")
        }

        val plantId = plantRepository.addPlant(plantReq.toEntity())
        println("✅ Plant created with ID: $plantId")

        val response = DataResponse(
            "success",
            "Berhasil menambahkan data tumbuhan",
            mapOf("plantId" to plantId)
        )
        call.respond(response)
    }

    suspend fun updatePlant(call: ApplicationCall) {
        val id = call.parameters["id"]
        println("📝 PUT /plants/$id")

        if (id == null) {
            throw AppException(400, "ID tumbuhan tidak boleh kosong!")
        }

        val oldPlant = plantRepository.getPlantById(id)
        if (oldPlant == null) {
            throw AppException(404, "Data tumbuhan tidak tersedia!")
        }

        val plantReq = getPlantRequest(call)

        if (plantReq.pathGambar.isEmpty()) {
            plantReq.pathGambar = oldPlant.pathGambar
            println("📸 Menggunakan gambar lama: ${plantReq.pathGambar}")
        }

        validatePlantRequest(plantReq, isUpdate = true)

        if (plantReq.nama != oldPlant.nama) {
            val existPlant = plantRepository.getPlantByName(plantReq.nama)
            if (existPlant != null) {
                if (plantReq.pathGambar != oldPlant.pathGambar) {
                    val tmpFile = File(plantReq.pathGambar)
                    if (tmpFile.exists()) {
                        tmpFile.delete()
                    }
                }
                throw AppException(409, "Tumbuhan dengan nama ini sudah terdaftar!")
            }
        }

        if (plantReq.pathGambar != oldPlant.pathGambar) {
            val oldFile = File(oldPlant.pathGambar)
            if (oldFile.exists()) {
                oldFile.delete()
                println("🗑️ Hapus gambar lama: ${oldPlant.pathGambar}")
            }
        }

        val isUpdated = plantRepository.updatePlant(id, plantReq.toEntity())
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui data tumbuhan!")
        }

        println("✅ Plant updated: $id")
        val response = DataResponse(
            "success",
            "Berhasil mengubah data tumbuhan",
            null
        )
        call.respond(response)
    }

    suspend fun deletePlant(call: ApplicationCall) {
        val id = call.parameters["id"]
        println("🗑️ DELETE /plants/$id")

        if (id == null) {
            throw AppException(400, "ID tumbuhan tidak boleh kosong!")
        }

        val oldPlant = plantRepository.getPlantById(id)
        if (oldPlant == null) {
            throw AppException(404, "Data tumbuhan tidak tersedia!")
        }

        val oldFile = File(oldPlant.pathGambar)

        val isDeleted = plantRepository.removePlant(id)
        if (!isDeleted) {
            throw AppException(400, "Gagal menghapus data tumbuhan!")
        }

        if (oldFile.exists()) {
            oldFile.delete()
            println("🗑️ Hapus file gambar: ${oldPlant.pathGambar}")
        }

        println("✅ Plant deleted: $id")
        val response = DataResponse(
            "success",
            "Berhasil menghapus data tumbuhan",
            null
        )
        call.respond(response)
    }

    suspend fun getPlantImage(call: ApplicationCall) {
        val id = call.parameters["id"]
        if (id == null) {
            return call.respond(HttpStatusCode.BadRequest)
        }

        val plant = plantRepository.getPlantById(id)
        if (plant == null) {
            return call.respond(HttpStatusCode.NotFound)
        }

        val file = File(plant.pathGambar)
        if (!file.exists()) {
            return call.respond(HttpStatusCode.NotFound)
        }

        call.respondFile(file)
    }
}