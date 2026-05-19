package com.example.nexusbank.core.network.di

import com.example.nexusbank.core.network.BuildConfig
import com.example.nexusbank.core.network.api.AuthApiService
import com.example.nexusbank.core.network.api.NexusBankApiService
import com.example.nexusbank.core.network.api.TransferApiService
import com.example.nexusbank.core.network.api.UserApiService
import com.example.nexusbank.core.network.interceptor.AuthInterceptor
import com.example.nexusbank.core.network.interceptor.ErrorHandlingInterceptor
import com.example.nexusbank.core.network.interceptor.TokenAuthenticator
import com.example.nexusbank.core.network.util.SSLPinningConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt graph for the network layer.
 *
 * Two parallel client stacks live here, keyed by qualifier:
 *
 *  - [Authenticated]  → adds `Authorization: Bearer …` via [AuthInterceptor]
 *                       and refreshes 401s via [TokenAuthenticator].
 *  - [Unauthenticated] → no auth header, no refresh retry. Used by
 *                       [AuthApiService] (login / OTP / refresh-token) so
 *                       a failing refresh can't trigger another refresh.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    // ── OkHttp clients ───────────────────────────────────────────

    @Provides
    @Singleton
    @Authenticated
    fun provideAuthenticatedOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        errorHandlingInterceptor: ErrorHandlingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            // Error mapping must run last on the response path, so add it
            // first — interceptors are invoked in registration order on
            // requests and reverse order on responses.
            .addInterceptor(errorHandlingInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        if (!BuildConfig.DEBUG) {
            builder.certificatePinner(SSLPinningConfig.createCertificatePinner())
        }
        return builder.build()
    }

    @Provides
    @Singleton
    @Unauthenticated
    fun provideUnauthenticatedOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        errorHandlingInterceptor: ErrorHandlingInterceptor,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(errorHandlingInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        // Cert pinning still applies — we just skip the auth headers.
        if (!BuildConfig.DEBUG) {
            builder.certificatePinner(SSLPinningConfig.createCertificatePinner())
        }
        return builder.build()
    }

    // ── Retrofit instances ───────────────────────────────────────

    @Provides
    @Singleton
    @Authenticated
    fun provideAuthenticatedRetrofit(
        @Authenticated okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    @Unauthenticated
    fun provideUnauthenticatedRetrofit(
        @Unauthenticated okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    // ── API services ─────────────────────────────────────────────

    /**
     * Auth endpoints (login, OTP, refresh) MUST use the unauthenticated
     * client. Otherwise [TokenAuthenticator] could fire a refresh while
     * the very refresh request is failing.
     */
    @Provides
    @Singleton
    fun provideAuthApiService(
        @Unauthenticated retrofit: Retrofit,
    ): AuthApiService = retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideNexusBankApiService(
        @Authenticated retrofit: Retrofit,
    ): NexusBankApiService = retrofit.create(NexusBankApiService::class.java)

    @Provides
    @Singleton
    fun provideTransferApiService(
        @Authenticated retrofit: Retrofit,
    ): TransferApiService = retrofit.create(TransferApiService::class.java)

    /**
     * User endpoints (me, logout) require an active session, so this service
     * lives on the @Authenticated client where [AuthInterceptor] adds the
     * Bearer header and [TokenAuthenticator] handles 401 refresh.
     */
    @Provides
    @Singleton
    fun provideUserApiService(
        @Authenticated retrofit: Retrofit,
    ): UserApiService = retrofit.create(UserApiService::class.java)

    private const val CONNECT_TIMEOUT_SECONDS = 30L
    private const val READ_TIMEOUT_SECONDS = 30L
    private const val WRITE_TIMEOUT_SECONDS = 30L
}
