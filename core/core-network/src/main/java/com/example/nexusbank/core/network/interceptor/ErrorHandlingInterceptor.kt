package com.example.nexusbank.core.network.interceptor

import com.example.nexusbank.core.network.error.HttpErrorParser
import com.example.nexusbank.core.network.error.NetworkException
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLException

/**
 * Centralised mapper for transport-level errors.
 *
 * - Wraps low-level [IOException]s coming out of the OkHttp chain into typed
 *   [NetworkException] subclasses so upper layers never see raw socket
 *   exceptions.
 * - Replaces non-2xx responses with a thrown [NetworkException.Http] that
 *   carries the parsed server message — callers using Retrofit `suspend`
 *   methods get a typed failure instead of having to inspect `Response`.
 *
 * Place this **first** in the interceptor chain so it observes the final
 * outcome of all other interceptors.
 */
@Singleton
class ErrorHandlingInterceptor @Inject constructor(
    private val errorParser: HttpErrorParser,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = try {
            chain.proceed(request)
        } catch (e: NetworkException) {
            // Already typed — let it propagate untouched.
            throw e
        } catch (e: SocketTimeoutException) {
            throw NetworkException.Timeout(e)
        } catch (e: UnknownHostException) {
            throw NetworkException.NoConnectivity(e)
        } catch (e: ConnectException) {
            throw NetworkException.NoConnectivity(e)
        } catch (e: SSLException) {
            throw NetworkException.Ssl(e)
        } catch (e: IOException) {
            throw NetworkException.Unknown(e)
        }

        if (!response.isSuccessful) {
            val parsedMessage = runCatching { errorParser.parseMessage(response) }.getOrNull()
            throw NetworkException.Http(
                code = response.code,
                serverMessage = parsedMessage,
                errorBody = response.body,
            )
        }
        return response
    }
}
