package tn.dadadrive.di

import tn.dadadrive.data.repositories.AuthRepositoryImpl
import tn.dadadrive.data.repositories.DriverRepositoryImpl
import tn.dadadrive.data.repositories.RidesRepositoryImpl
import tn.dadadrive.data.repositories.UserRepositoryImpl
import tn.dadadrive.data.repositories.WalletRepositoryImpl
import tn.dadadrive.domain.protocols.AuthRepository
import tn.dadadrive.domain.protocols.DriverRepository
import tn.dadadrive.domain.protocols.RidesRepository
import tn.dadadrive.domain.protocols.UserRepository
import tn.dadadrive.domain.protocols.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

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
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository
}
