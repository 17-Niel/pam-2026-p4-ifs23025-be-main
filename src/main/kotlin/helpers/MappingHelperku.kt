    package org.delcom.helpers

import kotlinx.coroutines.Dispatchers
import org.delcom.dao.SportDAO
import org.delcom.entities.Sport
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun daoToModel(dao: SportDAO) = Sport(
    dao.id.value.toString(),
    dao.nama,
    dao.pathGambar,
    dao.deskripsi,
    dao.kelebihan,
    dao.teknologi_yang_digunkan,
    dao.Tenaga,
    dao.createdAt,
    dao.updatedAt
)