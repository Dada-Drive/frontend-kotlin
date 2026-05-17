package tn.turbodrive.presentation.map

import android.media.RingtoneManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.turbodrive.R
import kotlinx.coroutines.delay
import tn.turbodrive.core.theme.LocalAppColors
import tn.turbodrive.domain.models.PassengerRideOffer
import java.util.Locale

@Composable
internal fun PassengerOfferCard(
    offer: PassengerRideOffer,
    isAccepting: Boolean,
    onRefuse: () -> Unit,
    onAccept: () -> Unit,
    onOfferExpired: () -> Unit,
) {
    val context = LocalContext.current
    val c = LocalAppColors.current
    var offerLifetimeProgress by remember(offer.id) { mutableStateOf(1f) }
    var offerExpired by remember(offer.id) { mutableStateOf(false) }

    val acceptEnabled = !isAccepting && !offerExpired

    val sheenTransition = rememberInfiniteTransition(label = "accept_btn_sheen")
    val sheenShift by sheenTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "sheen",
    )

    LaunchedEffect(offer.id) {
        runCatching {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone?.play()
        }
    }

    LaunchedEffect(offer.id, isAccepting, offerExpired) {
        if (isAccepting || offerExpired) return@LaunchedEffect
        val totalMs = 10_000L
        val stepMs = 100L
        val steps = (totalMs / stepMs).toInt()
        offerLifetimeProgress = 1f
        for (i in 1..steps) {
            offerLifetimeProgress = 1f - (i.toFloat() / steps.toFloat())
            delay(stepMs)
            if (isAccepting || offerExpired) return@LaunchedEffect
        }
        offerExpired = true
        onOfferExpired()
    }

    val ridesSegment =
        offer.totalRides?.let { count ->
            pluralStringResource(R.plurals.passenger_offer_rides, count, count)
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = c.surfaceMuted,
    ) {
        Column(Modifier.padding(10.dp)) {
            Text(
                text = String.format(Locale.US, "%.2f TND", offer.offeredFare),
                color = c.textPrimary,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text =
                    listOfNotNull(
                        offer.driverName,
                        offer.driverRating?.let { rating ->
                            "★ ${String.format(Locale.US, "%.1f", rating)}"
                        },
                        ridesSegment,
                    ).joinToString(" · "),
                color = c.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            offer.vehicleLabel?.let {
                Text(
                    text = it,
                    color = c.textSecondary,
                    fontSize = 12.sp,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier =
                        Modifier
                            .weight(1f)
                            .clickable(onClick = onRefuse),
                    shape = RoundedCornerShape(10.dp),
                    color = c.surface,
                ) {
                    Box(Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.driver_refuse), color = c.textSecondary, fontWeight = FontWeight.SemiBold)
                    }
                }
                val acceptInteraction = remember(offer.id) { MutableInteractionSource() }
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(10.dp)),
                ) {
                    Box(Modifier.fillMaxSize().background(Color.Black))
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(offerLifetimeProgress.coerceIn(0f, 1f))
                            .background(Color.White.copy(alpha = 0.14f))
                            .align(Alignment.CenterStart),
                    )
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors =
                                        listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.11f),
                                            Color.Transparent,
                                        ),
                                    start = Offset(260f * (sheenShift - 1f), 0f),
                                    end = Offset(260f * (sheenShift + 1f), 140f),
                                ),
                            ),
                    )
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text =
                                if (isAccepting) {
                                    stringResource(R.string.map_confirming_driver)
                                } else {
                                    stringResource(R.string.driver_accept)
                                },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    }
                    Box(
                        Modifier
                            .fillMaxSize()
                            .zIndex(1f)
                            .clickable(
                                enabled = acceptEnabled,
                                interactionSource = acceptInteraction,
                                indication = null,
                                onClick = onAccept,
                            ),
                    )
                }
            }
        }
    }
}

/** Barre du bas : ouvre la feuille « Enter your route » (champs From / To + carte). */
@Composable
internal fun RiderBottomRouteEntryBar(
    onOpenRouteSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    Surface(
        modifier =
            modifier
                .padding(bottom = 20.dp)
                .clickable { onOpenRouteSheet() },
        shape = RoundedCornerShape(32.dp),
        color = c.surfaceElevated,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = c.textSecondary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.map_where_to),
                color = c.textSecondary,
                fontSize = 16.sp,
            )
        }
    }
}
