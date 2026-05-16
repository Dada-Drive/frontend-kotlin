package tn.dadadrive.domain.protocols

import tn.dadadrive.domain.models.DriverRegistrationDraft

/**
 * Future-facing registration contract.
 * Backend endpoints are pending; implementation can be added once available.
 */
interface DriverRegistrationRepository {
    suspend fun submitRegistration(draft: DriverRegistrationDraft): Result<Unit>
}
