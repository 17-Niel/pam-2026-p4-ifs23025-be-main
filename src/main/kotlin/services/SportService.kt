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

    // Mengambil semua data mobil
    suspend fun getAllSports(call: ApplicationCall) {
        val search = call.request.queryParameters["search"] ?: ""
        val sports = sportRepository.getSports(search)

        val response = DataResponse(
            "success",
            "Berhasil mengambil daftar olahraga",
            mapOf("sports" to sports)
        )
        call.respond(response)
    }

    // Mengambil data olahraga berdasarkan id
    suspend fun getSportById(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID olahraga tidak boleh kosong!")

        val sport = sportRepository.getSportById(id)
            ?: throw AppException(404, "Data olahraga tidak tersedia!")

        val response = DataResponse(
            "success",
            "Berhasil mengambil data olahraga",
            mapOf("sport" to sport)
        )
        call.respond(response)
    }

    // Ambil data request multipart
    private suspend fun getSportRequest(call: ApplicationCall): SportRequest {
        val sportReq = SportRequest()

        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            when (part) {

                is PartData.FormItem -> {
                    when (part.name) {
                        "nama" -> sportReq.nama = part.value.trim()
                        "deskripsi" -> sportReq.deskripsi = part.value
                        "kelebihan" -> sportReq.kelebihan = part.value
                        "teknologi_yang_digunkan" -> sportReq.teknologi_yang_digunkan = part.value
                        "Tenaga" -> sportReq.Tenaga = part.value
                    }
                }

                is PartData.FileItem -> {
                    val ext = part.originalFileName
                        ?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" }
                        ?: ""

                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/sports/$fileName"

                    val file = File(filePath)
                    file.parentFile.mkdirs()

                    part.provider().copyAndClose(file.writeChannel())
                    sportReq.pathGambar = filePath
                }

                else -> {}
            }

            part.dispose()
        }

        return sportReq
    }

    // Validasi request
    private fun validateSportRequest(sportReq: SportRequest) {
        val validatorHelper = ValidatorHelper(sportReq.toMap())

        validatorHelper.required("nama", "Nama tidak boleh kosong")
        validatorHelper.required("deskripsi", "Deskripsi tidak boleh kosong")
        validatorHelper.required("kelebihan", "Kelebihan tidak boleh kosong")
        validatorHelper.required("teknologi_yang_digunkan", "Teknologi tidak boleh kosong")
        validatorHelper.required("Tenaga", "Tenaga tidak boleh kosong")
        validatorHelper.required("pathGambar", "Gambar tidak boleh kosong")

        validatorHelper.validate()

        val file = File(sportReq.pathGambar)
        if (!file.exists()) {
            throw AppException(400, "Gambar olahraga gagal diupload!")
        }
    }

    // Menambahkan data olahraga
    suspend fun createSport(call: ApplicationCall) {
        val sportReq = getSportRequest(call)
        validateSportRequest(sportReq)

        val existSport = sportRepository.getSportByName(sportReq.nama)
        if (existSport != null) {
            val tmpFile = File(sportReq.pathGambar)
            if (tmpFile.exists()) tmpFile.delete()
            throw AppException(409, "Olahraga dengan nama ini sudah terdaftar!")
        }

        val sportId = sportRepository.addSport(sportReq.toEntity())

        val response = DataResponse(
            "success",
            "Berhasil menambahkan data olahraga",
            mapOf("sportId" to sportId)
        )
        call.respond(response)
    }

    // Mengubah data olahraga
    suspend fun updateSport(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID olahraga tidak boleh kosong!")

        val oldSport = sportRepository.getSportById(id)
            ?: throw AppException(404, "Data olahraga tidak tersedia!")

        val sportReq = getSportRequest(call)

        if (sportReq.pathGambar.isEmpty()) {
            sportReq.pathGambar = oldSport.pathGambar
        }

        validateSportRequest(sportReq)

        if (sportReq.nama != oldSport.nama) {
            val existSport = sportRepository.getSportByName(sportReq.nama)
            if (existSport != null) {
                val tmpFile = File(sportReq.pathGambar)
                if (tmpFile.exists()) tmpFile.delete()
                throw AppException(409, "Olahraga dengan nama ini sudah terdaftar!")
            }
        }

        if (sportReq.pathGambar != oldSport.pathGambar) {
            val oldFile = File(oldSport.pathGambar)
            if (oldFile.exists()) oldFile.delete()
        }

        val isUpdated = sportRepository.updateSport(id, sportReq.toEntity())
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui data olahraga!")
        }

        val response = DataResponse(
            "success",
            "Berhasil mengubah data olahraga",
            null
        )
        call.respond(response)
    }

    // Menghapus data olahraga
    suspend fun deleteSport(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID olahraga tidak boleh kosong!")

        val oldSport = sportRepository.getSportById(id)
            ?: throw AppException(404, "Data olahraga tidak tersedia!")

        val oldFile = File(oldSport.pathGambar)

        val isDeleted = sportRepository.removeSport(id)
        if (!isDeleted) {
            throw AppException(400, "Gagal menghapus data olahraga!")
        }

        if (oldFile.exists()) oldFile.delete()

        val response = DataResponse(
            "success",
            "Berhasil menghapus data olahraga",
            null
        )
        call.respond(response)
    }

    // Mengambil gambar olahraga
    suspend fun getSportImage(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: return call.respond(HttpStatusCode.BadRequest)

        val sport = sportRepository.getSportById(id)
            ?: return call.respond(HttpStatusCode.NotFound)

        val file = File(sport.pathGambar)
        if (!file.exists()) {
            return call.respond(HttpStatusCode.NotFound)
        }

        call.respondFile(file)
    }
}