package tn.turbodrive.domain.models

/**
 * Contracts for the multi-step driver registration flow.
 * These models are intentionally decoupled from current backend endpoints.
 */
data class DriverRegistrationDraft(
    val personal: DriverPersonalInfoDraft,
    val license: DriverLicenseDraft,
    val vehicle: DriverVehicleDraft,
)

data class DriverPersonalInfoDraft(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val personalPhoto: DriverDocumentRef,
)

data class DriverLicenseDraft(
    val licenseNumber: String,
    val licenseExpiry: String,
    val licenseFrontPhoto: DriverDocumentRef,
    val licenseBackPhoto: DriverDocumentRef,
)

data class DriverVehicleDraft(
    val make: String,
    val model: String,
    val year: Int,
    val plateNumber: String,
    val color: String,
    val vehicleType: String,
    val vehiclePhoto: DriverDocumentRef,
    val registrationCertificatePhoto: DriverDocumentRef,
    val taxiLicenseFrontPhoto: DriverDocumentRef,
    val taxiLicenseBackPhoto: DriverDocumentRef,
)

/**
 * Temporary reference used before backend finalizes upload storage contract.
 */
data class DriverDocumentRef(
    val localUri: String,
    val mimeType: String? = null,
)
