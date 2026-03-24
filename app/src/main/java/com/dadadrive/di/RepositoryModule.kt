package com.dadadrive.di

import com.dadadrive.data.repository.FileRepositoryImpl
import com.dadadrive.data.repository.UserRepositoryImpl
import com.dadadrive.domain.repository.FileRepository
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
    abstract fun bindFileRepository(impl: FileRepositoryImpl): FileRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
