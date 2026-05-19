package tn.turbodrive.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tn.turbodrive.data.repositories.AuthRepositoryImpl
import tn.turbodrive.data.repositories.DocumentRepositoryImpl
import tn.turbodrive.data.repositories.DriverRepositoryImpl
import tn.turbodrive.data.repositories.NegotiationRepositoryImpl
import tn.turbodrive.data.repositories.RidesRepositoryImpl
import tn.turbodrive.data.repositories.UserRepositoryImpl
import tn.turbodrive.data.repositories.WalletRepositoryImpl
import tn.turbodrive.domain.protocols.AuthRepository
import tn.turbodrive.domain.protocols.DocumentRepository
import tn.turbodrive.domain.protocols.DriverRepository
import tn.turbodrive.domain.protocols.NegotiationRepository
import tn.turbodrive.domain.protocols.RidesRepository
import tn.turbodrive.domain.protocols.UserRepository
import tn.turbodrive.domain.protocols.WalletRepository
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

    @Binds
    @Singleton
    abstract fun bindDocumentRepository(impl: DocumentRepositoryImpl): DocumentRepository

    @Binds
    @Singleton
    abstract fun bindNegotiationRepository(impl: NegotiationRepositoryImpl): NegotiationRepository
}
