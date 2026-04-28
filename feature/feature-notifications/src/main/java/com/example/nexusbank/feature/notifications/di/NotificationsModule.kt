package com.example.nexusbank.feature.notifications.di

import com.example.nexusbank.feature.notifications.data.repository.NotificationRepositoryImpl
import com.example.nexusbank.feature.notifications.domain.repository.NotificationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository
}
