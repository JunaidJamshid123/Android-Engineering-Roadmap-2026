package com.example.nexusbank.feature.statement.data.api

import com.example.nexusbank.core.network.model.ApiResponse
import com.example.nexusbank.feature.statement.data.dto.StatementData
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface StatementApiService {

    @GET("statement/{accountId}")
    suspend fun getStatement(
        @Path("accountId") accountId: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<ApiResponse<StatementData>>

    /**
     * Downloads the statement as a binary file (PDF or CSV).
     * Use [Streaming] to avoid loading the entire payload into memory.
     */
    @Streaming
    @GET("statement/{accountId}/download")
    suspend fun downloadStatement(
        @Path("accountId") accountId: String,
        @Query("format") format: String = "pdf",
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<ResponseBody>
}
