package com.dadadrive.di

import com.dadadrive.data.repository.AuthRepositoryImpl
import com.dadadrive.data.repository.DriverRepositoryImpl
import com.dadadrive.domain.repository.AuthRepository
import com.dadadrive.domain.repository.DriverRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDriverRepository(impl: DriverRepositoryImpl): DriverRepository
}
