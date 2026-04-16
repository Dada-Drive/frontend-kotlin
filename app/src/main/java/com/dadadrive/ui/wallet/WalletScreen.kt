package com.dadadrive.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dadadrive.R
import com.dadadrive.ui.theme.LocalAppColors
import java.util.Locale

@Composable
fun WalletScreen(
    onBack: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val c = LocalAppColors.current
    val wallet by viewModel.wallet.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Surface(shape = CircleShape, color = c.surfaceMuted) {
                    Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = c.textHint)
                    }
                }
            }
            Text(
                text = stringResource(R.string.wallet_title),
                color = c.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = viewModel::refresh, enabled = !isLoading) {
                Surface(shape = CircleShape, color = c.surfaceMuted) {
                    Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = c.primary
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = c.primary)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = c.surfaceElevated
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = c.primary.copy(alpha = 0.15f)
                        ) {
                            Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = c.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    val isActive = wallet?.status.equals("active", ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (isActive) c.primary.copy(alpha = 0.16f) else c.errorRed.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = if (isActive) stringResource(R.string.wallet_status_active) else stringResource(R.string.wallet_status_suspended),
                            color = if (isActive) c.primary else c.errorRed,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.wallet_available_balance),
                    color = c.textSecondary,
                    fontSize = 14.sp
                )
                Text(
                    text = String.format(Locale.US, "%.2f TND", wallet?.balance ?: 0.0),
                    color = c.textPrimary,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Black
                )
                errorMessage?.let {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = c.dividerGrey.copy(alpha = 0.7f))
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = c.errorRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = stringResource(R.string.wallet_session_expired),
                                color = c.textSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = stringResource(R.string.wallet_retry),
                            color = c.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { viewModel.refresh() }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.wallet_transactions_title),
            color = c.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp
        )
        Spacer(Modifier.height(10.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = c.surfaceElevated
        ) {
            Column(Modifier.padding(14.dp)) {
                if (transactions.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = c.textHint.copy(alpha = 0.7f),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.wallet_no_transactions),
                            color = c.textHint,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    transactions.take(8).forEachIndexed { index, tx ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = tx.type.replace('_', ' '),
                                    color = c.textPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = tx.note ?: tx.status,
                                    color = c.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = String.format(Locale.US, "%.2f TND", tx.amount),
                                color = c.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        if (index < transactions.take(8).lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = c.dividerGrey
                            )
                        }
                    }
                }
            }
        }

    }
}
