package tn.dadadrive.di

import tn.dadadrive.data.storage.TokenManager
import tn.dadadrive.data.storage.TokenStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Binds
    @Singleton
    abstract fun bindTokenStorage(impl: TokenManager): TokenStorage
}
