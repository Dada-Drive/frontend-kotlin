package com.dadadrive.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadrive.R
import com.dadadrive.ui.theme.LocalAppColors

@Composable
internal fun PickTargetAddressBubble(address: String?, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    val label = address?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.map_move_to_set_pickup)
    val dark = isSystemInDarkTheme()
    val bg = if (dark) Color(0xE6000000) else Color(0xE6F2F2F2)
    val fg = if (dark) Color.White else c.textPrimary
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = bg,
        shadowElevation = 8.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            color = fg,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            maxLines = 2,
            textAlign = TextAlign.Center
        )
    }
}

/** Swift “From” row: green ring + dark center dot. */
@Composable
internal fun FromOriginRingIcon(modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Box(modifier.size(28.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .border(3.dp, c.primary, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(c.textPrimary, CircleShape)
        )
    }
}

@Composable
internal fun DestinationDotIcon(modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Box(
        modifier = modifier.size(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(c.errorRed, CircleShape)
        )
    }
}

@Composable
internal fun MapPickerRouteIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    IconButton(
        onClick = onClick,
        modifier = modifier.size(44.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF1B3D2F)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_map_picker_route),
                contentDescription = stringResource(R.string.map_pick_on_map),
                modifier = Modifier.size(22.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
internal fun RouteItinerarySheetContent(
    originValue: String,
    onOriginChange: (String) -> Unit,
    destinationValue: String,
    onDestinationChange: (String) -> Unit,
    pickupResults: List<AddressSearchHit>,
    pickupSearchLoading: Boolean,
    destinationResults: List<AddressSearchHit>,
    destinationSearchLoading: Boolean,
    onPickupHit: (AddressSearchHit) -> Unit,
    onDestinationHit: (AddressSearchHit) -> Unit,
    onClose: () -> Unit,
    onOpenMapPicker: () -> Unit
) {
    val c = LocalAppColors.current
    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        focusedTextColor = c.textPrimary,
        unfocusedTextColor = c.textPrimary,
        cursorColor = c.primary,
        focusedPlaceholderColor = c.textHint,
        unfocusedPlaceholderColor = c.textHint
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Enter your route",
                color = c.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClose) {
                Surface(
                    shape = CircleShape,
                    color = c.surfaceMuted,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = c.textHint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = c.surfaceMuted
        ) {
            Column(Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FromOriginRingIcon()
                    Spacer(Modifier.width(6.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.map_route_origin_hint),
                            color = c.textHint,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                        )
                        TextField(
                            value = originValue,
                            onValueChange = onOriginChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    stringResource(R.string.map_route_origin_placeholder),
                                    fontSize = 15.sp
                                )
                            },
                            singleLine = false,
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = fieldColors
                        )
                    }
                }

                RouteSheetSuggestionBlock(
                    results = pickupResults,
                    loading = pickupSearchLoading,
                    queryLengthOk = originValue.trim().length >= 3,
                    onHit = onPickupHit
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 52.dp, end = 16.dp),
                    color = c.dividerGrey.copy(alpha = 0.5f),
                    thickness = 1.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DestinationDotIcon()
                    Spacer(Modifier.width(6.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.map_route_destination_hint),
                            color = c.textHint,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                        )
                        TextField(
                            value = destinationValue,
                            onValueChange = onDestinationChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    stringResource(R.string.map_destination_placeholder),
                                    fontSize = 15.sp
                                )
                            },
                            singleLine = false,
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            colors = fieldColors
                        )
                    }
                    MapPickerRouteIconButton(onClick = onOpenMapPicker)
                }

                RouteSheetSuggestionBlock(
                    results = destinationResults,
                    loading = destinationSearchLoading,
                    queryLengthOk = destinationValue.trim().length >= 3,
                    onHit = onDestinationHit
                )
            }
        }
    }
}

@Composable
private fun RouteSheetSuggestionBlock(
    results: List<AddressSearchHit>,
    loading: Boolean,
    queryLengthOk: Boolean,
    onHit: (AddressSearchHit) -> Unit
) {
    val c = LocalAppColors.current
    when {
        loading -> {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = c.primary,
                    strokeWidth = 2.dp
                )
            }
        }
        results.isNotEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                results.forEachIndexed { index, hit ->
                    Text(
                        text = hit.label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onHit(hit) }
                            .padding(vertical = 10.dp),
                        color = c.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2
                    )
                    if (index < results.lastIndex) {
                        HorizontalDivider(color = c.dividerGrey.copy(alpha = 0.45f))
                    }
                }
            }
        }
        queryLengthOk -> {
            Text(
                text = "No address found — try other words.",
                color = c.textSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}
