package com.example.nexusbank.core.network.api

import com.example.nexusbank.core.network.model.ApiResponse
import com.example.nexusbank.core.network.model.MeResponseData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Endpoints that require an authenticated session.
 *
 * Kept separate from [AuthApiService] so the auth service can be bound to
 * the @Unauthenticated Retrofit (login / refresh-token must not trigger
 * the token authenticator on themselves) while these calls go through the
 * @Authenticated client and get a Bearer header attached automatically.
 */
interface UserApiService {

    @GET("auth/me")
    suspend fun getMe(): Response<ApiResponse<MeResponseData>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>
}
