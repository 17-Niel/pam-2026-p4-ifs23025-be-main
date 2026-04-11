package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.copyAndClose
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.SportRequest
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.ISportRepository
import java.io.File
import java.util.*

class SportService(private val sportRepository: ISportRepository) {

    suspend fun getAllSports(call: ApplicationCall) {
        try {
            val search = call.request.queryParameters["search"] ?: ""
            println("📋 GET /sports - search: $search")

            val sports = sportRepository.getSports(search)
            println("📋 Ditemukan ${sports.size} Mobil Sport")

            val response = DataResponse(
                "success",
                "Berhasil mengambil daftar Mobil Sport",
                mapOf("sports" to sports)
            )
            call.respond(response)
        } catch (e: Exception) {
            println("❌ Error getAllSports: ${e.message}")
            throw e
        }
    }

    suspend fun getSportById(call: ApplicationCall) {
        val id = call.parameters["id"]
        println("📋 GET /sports/$id")

        if (id == null) {
            throw AppException(400, "ID Mobil Sport tidak boleh kosong!")
        }

        val sport = sportRepository.getSportById(id)
        if (sport == null) {
            throw AppException(404, "Data Mobil Sport tidak tersedia!")
        }

        val response = DataResponse(
            "success",
            "Berhasil mengambil data Mobil Sport",
            mapOf("sport" to sport)
        )
        call.respond(response)
    }

    private suspend fun getSportRequest(call: ApplicationCall): SportRequest {
        val sportReq = SportRequest()

        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 10)
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "nama" -> sportReq.nama = part.value.trim()
                        "deskripsi" -> sportReq.deskripsi = part.value
                        "kelebihan" -> sportReq.kelebihan = part.value
                        "teknologi" -> sportReq.teknologi = part.value
                        "tenaga" -> sportReq.tenaga = part.value
                    }
                    println("📝 Field ${part.name}: ${part.value.take(50)}")
                }

                is PartData.FileItem -> {
                    val originalName = part.originalFileName ?: "file"
                    val ext = originalName.substringAfterLast('.', "")
                    val fileName = UUID.randomUUID().toString() + if (ext.isNotEmpty()) ".$ext" else ""
                    val filePath = "uploads/sports/$fileName"

                    val file = File(filePath)
                    file.parentFile.mkdirs()

                    part.provider().copyAndClose(file.writeChannel())
                    sportReq.pathGambar = filePath
                    println("📸 Upload gambar: $filePath")
                }

                else -> {}
            }

            part.dispose()
        }

        return sportReq
    }

    private fun validateSportRequest(sportReq: SportRequest, isUpdate: Boolean = false) {
        val validatorHelper = ValidatorHelper(sportReq.toMap())

        validatorHelper.required("nama", "Nama tidak boleh kosong")
        validatorHelper.required("deskripsi", "Deskripsi tidak boleh kosong")
        validatorHelper.required("kelebihan", "Kelebihan tidak boleh kosong")
        validatorHelper.required("teknologi", "Teknologi tidak boleh kosong")
        validatorHelper.required("tenaga", "Tenaga tidak boleh kosong")

        if (!isUpdate || sportReq.pathGambar.isNotEmpty()) {
            validatorHelper.required("pathGambar", "Gambar tidak boleh kosong")
        }

        validatorHelper.validate()

        if (sportReq.pathGambar.isNotEmpty()) {
            val file = File(sportReq.pathGambar)
            if (!file.exists()) {
                throw AppException(400, "Gambar Mobil Sport gagal diupload!")
            }
        }
    }

    suspend fun createSport(call: ApplicationCall) {
        println("📝 POST /sports - Create new sport")
        val sportReq = getSportRequest(call)
        validateSportRequest(sportReq, isUpdate = false)

        val existSport = sportRepository.getSportByName(sportReq.nama)
        if (existSport != null) {
            val tmpFile = File(sportReq.pathGambar)
            if (tmpFile.exists()) tmpFile.delete()
            throw AppException(409, "Mobil Sport dengan nama ini sudah terdaftar!")
        }

        val sportId = sportRepository.addSport(sportReq.toEntity())
        println("✅ Sport created with ID: $sportId")

        val response = DataResponse(
            "success",
            "Berhasil menambahkan data Mobil Sport",
            mapOf("sportId" to sportId)
        )
        call.respond(response)
    }

    suspend fun updateSport(call: ApplicationCall) {
        val id = call.parameters["id"]
        println("📝 PUT /sports/$id")

        if (id == null) {
            throw AppException(400, "ID Mobil Sport tidak boleh kosong!")
        }

        val oldSport = sportRepository.getSportById(id)
        if (oldSport == null) {
            throw AppException(404, "Data Mobil Sport tidak tersedia!")
        }

        val sportReq = getSportRequest(call)

        if (sportReq.pathGambar.isEmpty()) {
            sportReq.pathGambar = oldSport.pathGambar
            println("📸 Menggunakan gambar lama: ${sportReq.pathGambar}")
        }

        validateSportRequest(sportReq, isUpdate = true)

        if (sportReq.nama != oldSport.nama) {
            val existSport = sportRepository.getSportByName(sportReq.nama)
            if (existSport != null) {
                if (sportReq.pathGambar != oldSport.pathGambar) {
                    val tmpFile = File(sportReq.pathGambar)
                    if (tmpFile.exists()) tmpFile.delete()
                }
                throw AppException(409, "Mobil Sport dengan nama ini sudah terdaftar!")
            }
        }

        if (sportReq.pathGambar != oldSport.pathGambar) {
            val oldFile = File(oldSport.pathGambar)
            if (oldFile.exists()) {
                oldFile.delete()
                println("🗑️ Hapus gambar lama: ${oldSport.pathGambar}")
            }
        }

        val isUpdated = sportRepository.updateSport(id, sportReq.toEntity())
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui data Mobil Sport!")
        }

        println("✅ Sport updated: $id")
        val response = DataResponse(
            "success",
            "Berhasil mengubah data Mobil Sport",
            null
        )
        call.respond(response)
    }

    suspend fun deleteSport(call: ApplicationCall) {
        val id = call.parameters["id"]
        println("🗑️ DELETE /sports/$id")

        if (id == null) {
            throw AppException(400, "ID Mobil Sport tidak boleh kosong!")
        }

        val oldSport = sportRepository.getSportById(id)
        if (oldSport == null) {
            throw AppException(404, "Data Mobil Sport tidak tersedia!")
        }

        val oldFile = File(oldSport.pathGambar)

        val isDeleted = sportRepository.removeSport(id)
        if (!isDeleted) {
            throw AppException(400, "Gagal menghapus data Mobil Sport!")
        }

        if (oldFile.exists()) {
            oldFile.delete()
            println("🗑️ Hapus file gambar: ${oldSport.pathGambar}")
        }

        println("✅ Sport deleted: $id")
        val response = DataResponse(
            "success",
            "Berhasil menghapus data Mobil Sport",
            null
        )
        call.respond(response)
    }

    suspend fun getSportImage(call: ApplicationCall) {
        val id = call.parameters["id"]
        if (id == null) {
            return call.respond(HttpStatusCode.BadRequest)
        }

        val sport = sportRepository.getSportById(id)
        if (sport == null) {
            return call.respond(HttpStatusCode.NotFound)
        }

        val file = File(sport.pathGambar)
        if (!file.exists()) {
            return call.respond(HttpStatusCode.NotFound)
        }

        call.respondFile(file)
    }
}