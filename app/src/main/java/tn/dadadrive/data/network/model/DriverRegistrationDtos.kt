package tn.dadadrive.data.network.model

import com.google.gson.annotations.SerializedName

data class DriverRegistrationRequestDto(
    @SerializedName("personal_info") val personalInfo: DriverPersonalInfoDto,
    @SerializedName("license_info") val licenseInfo: DriverLicenseInfoDto,
    @SerializedName("vehicle_info") val vehicleInfo: DriverVehicleInfoDto,
)

data class DriverPersonalInfoDto(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("date_of_birth") val dateOfBirth: String,
    @SerializedName("personal_photo") val personalPhoto: DriverDocumentDto,
)

data class DriverLicenseInfoDto(
    @SerializedName("license_number") val licenseNumber: String,
    @SerializedName("license_expiry") val licenseExpiry: String,
    @SerializedName("license_front_photo") val licenseFrontPhoto: DriverDocumentDto,
    @SerializedName("license_back_photo") val licenseBackPhoto: DriverDocumentDto,
)

data class DriverVehicleInfoDto(
    val make: String,
    val model: String,
    val year: Int,
    @SerializedName("plate_number") val plateNumber: String,
    val color: String,
    @SerializedName("vehicle_type") val vehicleType: String,
    @SerializedName("vehicle_photo") val vehiclePhoto: DriverDocumentDto,
    @SerializedName("registration_certificate_photo") val registrationCertificatePhoto: DriverDocumentDto,
    @SerializedName("taxi_license_front_photo") val taxiLicenseFrontPhoto: DriverDocumentDto,
    @SerializedName("taxi_license_back_photo") val taxiLicenseBackPhoto: DriverDocumentDto,
)

data class DriverDocumentDto(
    @SerializedName("local_uri") val localUri: String,
    @SerializedName("mime_type") val mimeType: String? = null,
)

/** Payload de `POST /driver/registration` — encapsulé par `ApiResponse<DriverRegistrationResponseDto>`. */
data class DriverRegistrationResponseDto(
    val message: String? = null,
)
