package tn.dadadrive.data.network

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdempotencyKeyGenerator @Inject constructor() {
    fun newKey(): String = UUID.randomUUID().toString()
}
