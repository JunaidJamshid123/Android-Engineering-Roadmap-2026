package com.example.nexusbank.core.common.dispatcher

import javax.inject.Qualifier

/**
 * Qualifier for injecting a specific [kotlinx.coroutines.CoroutineDispatcher].
 *
 * Usage:
 * ```
 * class Repo @Inject constructor(
 *     @Dispatcher(NexusBankDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
 * )
 * ```
 *
 * Prefer injecting [DispatcherProvider] when a class needs more than one
 * dispatcher or wants easier test substitution.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val value: NexusBankDispatchers)

enum class NexusBankDispatchers { Default, IO, Main }
