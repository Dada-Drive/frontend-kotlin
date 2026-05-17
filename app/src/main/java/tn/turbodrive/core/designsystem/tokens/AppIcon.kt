package tn.turbodrive.core.designsystem.tokens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Semantic icon mapping (design-system.md §8) — Material Symbols subset.
 */
object AppIcon {
    val back: ImageVector get() = Icons.AutoMirrored.Filled.ArrowBack
    val close: ImageVector get() = Icons.Filled.Close
    val person: ImageVector get() = Icons.Filled.Person
    val notifications: ImageVector get() = Icons.Filled.Notifications
    val message: ImageVector get() = Icons.AutoMirrored.Filled.Message
    val call: ImageVector get() = Icons.Filled.Call
    val location: ImageVector get() = Icons.Filled.LocationOn
    val checkCircle: ImageVector get() = Icons.Filled.CheckCircle
    val errorOutline: ImageVector get() = Icons.Filled.ErrorOutline
    val warning: ImageVector get() = Icons.Filled.Warning
    val info: ImageVector get() = Icons.Filled.Info
    val wallet: ImageVector get() = Icons.Filled.AccountBalanceWallet
    val wifiOff: ImageVector get() = Icons.Filled.WifiOff
}
