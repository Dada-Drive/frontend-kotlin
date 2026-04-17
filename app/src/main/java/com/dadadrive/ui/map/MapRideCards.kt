package com.dadadrive.ui.map

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import com.dadadrive.R
import com.dadadrive.domain.model.PassengerRideOffer
import com.dadadrive.ui.theme.LocalAppColors
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
internal fun PassengerOfferCard(
    offer: PassengerRideOffer,
    isAccepting: Boolean,
    onRefuse: () -> Unit,
    onAccept: () -> Unit
) {
    val c = LocalAppColors.current
    var localAcceptProgress by remember(offer.id) { mutableStateOf(0f) }
    var localRunning by remember(offer.id) { mutableStateOf(false) }
    val displayedProgress by animateFloatAsState(
        targetValue = localAcceptProgress,
        animationSpec = tween(durationMillis = 120),
        label = "accept_progress"
    )
    val acceptProgressColor = lerp(c.surfaceMuted, c.primary, displayedProgress.coerceIn(0f, 1f))
    val acceptEnabled = !isAccepting && !localRunning
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = c.surfaceMuted
    ) {
        Column(Modifier.padding(10.dp)) {
            Text(
                text = String.format(Locale.US, "%.2f TND", offer.offeredFare),
                color = c.textPrimary,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = listOfNotNull(
                    offer.driverName,
                    offer.driverRating?.let { "★ ${String.format(Locale.US, "%.1f", it)}" },
                    offer.totalRides?.let { "$it rides" }
                ).joinToString(" "),
                color = c.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            offer.vehicleLabel?.let {
                Text(
                    text = it,
                    color = c.textSecondary,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onRefuse),
                    shape = RoundedCornerShape(10.dp),
                    color = c.surface
                ) {
                    Box(Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.driver_refuse), color = c.textSecondary, fontWeight = FontWeight.SemiBold)
                    }
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = acceptEnabled) {
                            if (localRunning) return@clickable
                            localRunning = true
                            localAcceptProgress = 0f
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = acceptProgressColor
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAccepting || localRunning) {
                                stringResource(R.string.map_accepting_with_progress, (displayedProgress * 100f).toInt())
                            } else {
                                stringResource(R.string.driver_accept)
                            },
                            color = c.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (localRunning && !isAccepting) {
                LaunchedEffect(offer.id, localRunning) {
                    val totalMs = 2200L
                    val stepMs = 100L
                    val steps = (totalMs / stepMs).toInt()
                    for (i in 1..steps) {
                        localAcceptProgress = i.toFloat() / steps.toFloat()
                        delay(stepMs)
                    }
                    onAccept()
                    localRunning = false
                    localAcceptProgress = 0f
                }
            }
        }
    }
}

/** Barre du bas : ouvre la feuille « Enter your route » (champs From / To + carte). */
@Composable
internal fun RiderBottomRouteEntryBar(
    onOpenRouteSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    Surface(
        modifier = modifier
            .padding(bottom = 20.dp)
            .clickable { onOpenRouteSheet() },
        shape = RoundedCornerShape(32.dp),
        color = c.surfaceElevated,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = c.textSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.map_where_to),
                color = c.textSecondary,
                fontSize = 16.sp
            )
        }
    }
}
