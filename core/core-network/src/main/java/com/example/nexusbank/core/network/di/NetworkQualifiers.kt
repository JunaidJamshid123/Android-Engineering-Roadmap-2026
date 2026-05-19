package com.example.nexusbank.core.network.di

import javax.inject.Qualifier

/**
 * Marks the OkHttp/Retrofit instance configured with [AuthInterceptor] +
 * [TokenAuthenticator]. Use for any API that requires the user to be
 * signed in (accounts, transfers, cards, statement, profile, …).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Authenticated

/**
 * Marks the OkHttp/Retrofit instance with no auth header injection and
 * no 401 → refresh-token retry loop. Use for endpoints that issue or
 * exchange credentials themselves — login, OTP, refresh-token — to
 * avoid recursive refresh attempts on the very call that's refreshing.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Unauthenticated
