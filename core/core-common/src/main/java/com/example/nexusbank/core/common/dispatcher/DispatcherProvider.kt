package com.example.nexusbank.core.common.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Abstraction over [kotlinx.coroutines.Dispatchers] so coroutine-using code
 * (ViewModels, use cases, repositories) can be unit-tested with a
 * `TestDispatcher` injected in place of the real ones.
 *
 * Inject this everywhere instead of referencing [Dispatchers] directly.
 * Never use `GlobalScope` or `runBlocking` in production code — launch on
 * `viewModelScope` / `lifecycleScope` and switch threads via these
 * dispatchers using `withContext(dispatchers.io) { ... }`.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val mainImmediate: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}

/** Production implementation backed by [Dispatchers]. */
@Singleton
class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val mainImmediate: CoroutineDispatcher = Dispatchers.Main.immediate
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}
