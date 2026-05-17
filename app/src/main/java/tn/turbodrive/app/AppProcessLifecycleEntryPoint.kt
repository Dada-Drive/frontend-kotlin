package tn.turbodrive.app

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppProcessLifecycleEntryPoint {
    fun appProcessLifecycleBridge(): AppProcessLifecycleBridge
}
