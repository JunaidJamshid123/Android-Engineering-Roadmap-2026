package com.example.nexusbank.core.data.util

import com.example.nexusbank.core.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Implements offline-first pattern:
 * 1. Emit cached data from local DB
 * 2. Fetch from network
 * 3. Save network result to DB
 * 4. Emit updated data from DB
 */
inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType) -> Boolean = { true }
): Flow<Resource<ResultType>> = flow {
    emit(Resource.Loading)

    val data = query().first()

    val flow = if (shouldFetch(data)) {
        emit(Resource.Loading)
        try {
            saveFetchResult(fetch())
            query().map { Resource.Success(it) }
        } catch (throwable: Throwable) {
            query().map {
                Resource.Error(
                    message = throwable.localizedMessage ?: "Unknown error",
                )
            }
        }
    } else {
        query().map { Resource.Success(it) }
    }

    emitAll(flow)
}
