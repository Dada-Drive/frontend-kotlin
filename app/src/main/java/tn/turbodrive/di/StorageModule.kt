package tn.turbodrive.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tn.turbodrive.data.storage.TokenManager
import tn.turbodrive.data.storage.TokenStorage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {
    @Binds
    @Singleton
    abstract fun bindTokenStorage(impl: TokenManager): TokenStorage
}
