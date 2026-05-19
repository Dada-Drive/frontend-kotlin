package tn.turbodrive.domain.models

data class ResyncResult(
    val activeRide: ActiveRide?,
    val walletInfo: WalletInfo?,
)
