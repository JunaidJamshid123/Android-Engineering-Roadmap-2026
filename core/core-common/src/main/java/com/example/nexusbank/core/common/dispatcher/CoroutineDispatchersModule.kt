package com.example.nexusbank.core.common.dispatcher

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

/**
 * Hilt bindings for coroutine dispatchers and [DispatcherProvider].
 *
 * - Inject [DispatcherProvider] for ergonomic access to all dispatchers.
 * - Inject `@Dispatcher(NexusBankDispatchers.IO) CoroutineDispatcher` etc.
 *   when only one dispatcher is needed (keeps constructors minimal).
 */
@Module
@InstallIn(SingletonComponent::class)
object CoroutineDispatchersModule {

    @Provides
    @Dispatcher(NexusBankDispatchers.IO)
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(NexusBankDispatchers.Default)
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Dispatcher(NexusBankDispatchers.Main)
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DispatcherProviderModule {

    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider
}
