package tn.turbodrive.domain.usecases

import tn.turbodrive.data.network.model.SendOtpResponse
import tn.turbodrive.domain.protocols.AuthRepository
import javax.inject.Inject

class SendOtpUseCase
    @Inject
    constructor(private val repository: AuthRepository) {
        suspend operator fun invoke(phone: String): Result<SendOtpResponse> {
            if (phone.isBlank()) {
                return Result.failure(IllegalArgumentException("Numéro de téléphone requis"))
            }
            return repository.sendOtp(phone)
        }
    }
