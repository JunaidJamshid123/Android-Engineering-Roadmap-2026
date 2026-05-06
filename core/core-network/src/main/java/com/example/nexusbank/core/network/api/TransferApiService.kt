package com.example.nexusbank.core.network.api

import com.example.nexusbank.core.network.model.ApiResponse
import com.example.nexusbank.core.network.model.ResolveRecipientData
import com.example.nexusbank.core.network.model.ResolveRecipientRequest
import com.example.nexusbank.core.network.model.TransferHistoryData
import com.example.nexusbank.core.network.model.TransferRequest
import com.example.nexusbank.core.network.model.TransferResponseData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TransferApiService {

    @POST("transfer/resolve-recipient")
    suspend fun resolveRecipient(
        @Body request: ResolveRecipientRequest
    ): Response<ApiResponse<ResolveRecipientData>>

    @POST("transfer")
    suspend fun executeTransfer(
        @Body request: TransferRequest
    ): Response<ApiResponse<TransferResponseData>>

    @GET("transfer")
    suspend fun getTransferHistory(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<ApiResponse<TransferHistoryData>>
}
