package com.dadadrive.di

import com.dadadrive.data.repository.AuthRepositoryImpl
import com.dadadrive.data.repository.RideRepositoryImpl
import com.dadadrive.data.repository.UserRepositoryImpl
import com.dadadrive.domain.repository.AuthRepository
import com.dadadrive.domain.repository.RideRepository
import com.dadadrive.domain.repository.UserRepository
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
    abstract fun bindRideRepository(impl: RideRepositoryImpl): RideRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
