package tn.turbodrive.domain.protocols

import tn.turbodrive.domain.models.DriverRegistrationDraft

/**
 * Future-facing registration contract.
 * Backend endpoints are pending; implementation can be added once available.
 */
interface DriverRegistrationRepository {
    suspend fun submitRegistration(draft: DriverRegistrationDraft): Result<Unit>
}
