package com.example.nexusbank.feature.statement.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.core.network.api.UserApiService
import com.example.nexusbank.core.network.util.NetworkResult
import com.example.nexusbank.core.network.util.safeApiCall
import com.example.nexusbank.feature.statement.data.api.StatementApiService
import com.example.nexusbank.feature.statement.data.dto.StatementAccountDto
import com.example.nexusbank.feature.statement.data.dto.StatementData
import com.example.nexusbank.feature.statement.data.dto.StatementPaginationDto
import com.example.nexusbank.feature.statement.data.dto.StatementSummaryDto
import com.example.nexusbank.feature.statement.data.dto.StatementTxnDto
import com.example.nexusbank.feature.statement.domain.model.Statement
import com.example.nexusbank.feature.statement.domain.model.StatementAccount
import com.example.nexusbank.feature.statement.domain.model.StatementPagination
import com.example.nexusbank.feature.statement.domain.model.StatementSummary
import com.example.nexusbank.feature.statement.domain.model.StatementTxn
import com.example.nexusbank.feature.statement.domain.model.TxnDirection
import com.example.nexusbank.feature.statement.domain.repository.StatementRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatementRepositoryImpl @Inject constructor(
    private val api: StatementApiService,
    private val userApi: UserApiService,
    @ApplicationContext private val context: Context
) : StatementRepository {

    override suspend fun getStatement(
        accountId: String?,
        from: String?,
        to: String?,
        page: Int?,
        limit: Int?
    ): Resource<Statement> {
        // Resolve account if not provided.
        val resolvedAccountId = accountId ?: when (val r = resolvePrimaryAccount()) {
            is Resource.Success -> r.data
            is Resource.Error -> return Resource.Error(r.message, r.code)
            is Resource.Loading -> return Resource.Error("Unexpected loading")
        }

        return when (val res = safeApiCall {
            api.getStatement(resolvedAccountId, from, to, page, limit)
        }) {
            is NetworkResult.Success -> {
                val body = res.data
                val data = body.data
                if (body.success && data != null) {
                    Resource.Success(data.toDomain())
                } else {
                    Resource.Error(body.message ?: "Failed to load statement")
                }
            }
            is NetworkResult.Error -> Resource.Error(res.message, res.code)
        }
    }

    override suspend fun downloadStatement(
        accountId: String?,
        format: String,
        from: String?,
        to: String?
    ): Resource<Uri> {
        val resolvedAccountId = accountId ?: when (val r = resolvePrimaryAccount()) {
            is Resource.Success -> r.data
            is Resource.Error -> return Resource.Error(r.message, r.code)
            is Resource.Loading -> return Resource.Error("Unexpected loading")
        }

        val response = try {
            api.downloadStatement(resolvedAccountId, format, from, to)
        } catch (e: Exception) {
            return Resource.Error(e.message ?: "Network error")
        }

        if (!response.isSuccessful) {
            return Resource.Error(
                "Download failed (${response.code()})",
                response.code()
            )
        }
        val body = response.body()
            ?: return Resource.Error("Empty response body")

        return try {
            val uri = withContext(Dispatchers.IO) {
                saveToDownloads(body, format)
            }
            Resource.Success(uri)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Could not save file")
        }
    }

    private fun saveToDownloads(body: ResponseBody, format: String): Uri {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val ext = if (format.equals("csv", ignoreCase = true)) "csv" else "pdf"
        val mime = if (ext == "csv") "text/csv" else "application/pdf"
        val fileName = "statement_$timestamp.$ext"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mime)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/NexusBank")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val uri = resolver.insert(collection, values)
                ?: throw IllegalStateException("Failed to create file in Downloads")
            resolver.openOutputStream(uri).use { out ->
                requireNotNull(out) { "Failed to open output stream" }
                body.byteStream().use { input -> input.copyTo(out) }
            }
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            uri
        } else {
            val dir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "NexusBank"
            ).apply { if (!exists()) mkdirs() }
            val file = File(dir, fileName)
            FileOutputStream(file).use { out ->
                body.byteStream().use { input -> input.copyTo(out) }
            }
            // Expose via FileProvider would require manifest setup; return a plain file Uri.
            Uri.fromFile(file)
        }
    }

    private suspend fun resolvePrimaryAccount(): Resource<String> {
        return when (val r = safeApiCall { userApi.getMe() }) {
            is NetworkResult.Success -> {
                val body = r.data
                val first = body.data?.bankAccounts?.firstOrNull()
                if (body.success && first != null) {
                    Resource.Success(first.id)
                } else {
                    Resource.Error(body.message ?: "No account found")
                }
            }
            is NetworkResult.Error -> Resource.Error(r.message, r.code)
        }
    }
}

// ── Mappers ──

private fun StatementData.toDomain() = Statement(
    account = account.toDomain(),
    periodFrom = period.from,
    periodTo = period.to,
    openingBalance = openingBalance,
    closingBalance = closingBalance,
    currentBalance = currentBalance,
    summary = summary.toDomain(),
    pagination = pagination.toDomain(),
    transactions = transactions.map { it.toDomain() }
)

private fun StatementAccountDto.toDomain() = StatementAccount(
    id = id,
    accountNumberMasked = accountNumberMasked,
    accountType = accountType,
    currency = currency,
    status = status,
    holderName = holderName
)

private fun StatementSummaryDto.toDomain() = StatementSummary(
    totalCredits = totalCredits,
    totalDebits = totalDebits,
    totalFees = totalFees,
    net = net,
    count = count
)

private fun StatementPaginationDto.toDomain() = StatementPagination(
    page = page, limit = limit, total = total, totalPages = totalPages
)

private fun StatementTxnDto.toDomain() = StatementTxn(
    id = id,
    referenceNumber = referenceNumber,
    date = date,
    direction = runCatching { TxnDirection.valueOf(direction) }.getOrDefault(TxnDirection.DEBIT),
    amount = amount,
    fee = fee,
    netAmount = netAmount,
    currency = currency,
    runningBalance = runningBalance,
    status = status,
    counterpartyName = counterpartyName,
    counterpartyAccountMasked = counterpartyAccountNumberMasked,
    purpose = purpose,
    remarks = remarks
)
