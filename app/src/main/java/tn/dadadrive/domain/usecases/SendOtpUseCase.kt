package tn.dadadrive.domain.usecases

import tn.dadadrive.data.network.model.SendOtpResponse
import tn.dadadrive.domain.protocols.AuthRepository
import javax.inject.Inject

class SendOtpUseCase @Inject constructor(private val repository: AuthRepository) {

    suspend operator fun invoke(phone: String): Result<SendOtpResponse> {
        if (phone.isBlank())
            return Result.failure(IllegalArgumentException("Numéro de téléphone requis"))
        return repository.sendOtp(phone)
    }
}
