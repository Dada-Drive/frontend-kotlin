package com.dadadrive.di

import com.dadadrive.data.repository.AuthRepositoryImpl
import com.dadadrive.data.repository.DriverRepositoryImpl
import com.dadadrive.data.repository.RidesRepositoryImpl
import com.dadadrive.data.repository.WalletRepositoryImpl
import com.dadadrive.domain.repository.AuthRepository
import com.dadadrive.domain.repository.DriverRepository
import com.dadadrive.domain.repository.RidesRepository
import com.dadadrive.domain.repository.WalletRepository
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

    @Binds
    @Singleton
    abstract fun bindRidesRepository(impl: RidesRepositoryImpl): RidesRepository

    @Binds
    @Singleton
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository
}
