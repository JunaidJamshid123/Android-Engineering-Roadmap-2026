package com.example.nexusbank.core.network.api

import com.example.nexusbank.core.network.model.*
import retrofit2.Response
import retrofit2.http.*

interface NexusBankApiService {

    // ── User ──
    @GET("user/profile")
    suspend fun getProfile(): Response<UserResponse>

    @PUT("user/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserResponse>

    // ── Accounts ──
    @GET("accounts")
    suspend fun getAccounts(): Response<AccountsResponse>

    @GET("accounts/{id}")
    suspend fun getAccount(@Path("id") accountId: String): Response<AccountResponse>

    @GET("accounts/{id}/statement")
    suspend fun getStatement(
        @Path("id") accountId: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<TransactionsResponse>

    @GET("accounts/{id}/mini-statement")
    suspend fun getMiniStatement(
        @Path("id") accountId: String
    ): Response<TransactionsResponse>

    // ── Transactions ──
    @GET("transactions/{id}")
    suspend fun getTransaction(@Path("id") txnId: String): Response<TransactionResponse>

    // ── Cards ──
    @GET("cards")
    suspend fun getCards(): Response<CardsResponse>

    @GET("cards/{id}")
    suspend fun getCard(@Path("id") cardId: String): Response<CardResponse>

    @PUT("cards/{id}/lock")
    suspend fun lockCard(
        @Path("id") cardId: String,
        @Body body: Map<String, Boolean>
    ): Response<CardResponse>

    @PUT("cards/{id}/online")
    suspend fun toggleOnline(
        @Path("id") cardId: String,
        @Body body: Map<String, Boolean>
    ): Response<CardResponse>

    @PUT("cards/{id}/international")
    suspend fun toggleInternational(
        @Path("id") cardId: String,
        @Body body: Map<String, Boolean>
    ): Response<CardResponse>
}
