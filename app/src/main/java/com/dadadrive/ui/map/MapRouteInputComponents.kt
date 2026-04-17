package com.dadadrive.ui.map

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadrive.R
import com.dadadrive.ui.theme.LocalAppColors

@Composable
internal fun RouteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isActive: Boolean,
    isOrigin: Boolean,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    focusRequester: FocusRequester? = null,
    interactionSource: MutableInteractionSource,
    fieldColors: androidx.compose.material3.TextFieldColors,
    imeAction: ImeAction = ImeAction.Next,
    onMapPickerClick: () -> Unit,
    cachedMiniMap: ImageBitmap?,
    onFocused: () -> Unit = {},
    showRemove: Boolean = false,
    onRemoveClick: (() -> Unit)? = null,
    accentColorOverride: Color? = null,
    intermediateStopNumber: Int? = null,
    showMapPicker: Boolean = true,
    staticBorderColor: Color? = null,
    staticBackgroundColor: Color? = null,
    staticTextColor: Color? = null,
    disableShadow: Boolean = false
) {
    val c = LocalAppColors.current
    val accentColor = accentColorOverride ?: if (isOrigin) c.primary else c.errorRed
    val borderColor by animateColorAsState(
        targetValue = if (isActive) accentColor.copy(alpha = 0.45f) else c.dividerGrey.copy(alpha = 0.55f),
        animationSpec = tween(220), label = "border_color"
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isActive) 4.dp else 1.dp,
        animationSpec = tween(220), label = "shadow_elev"
    )
    val searchIconColor by animateColorAsState(
        targetValue = if (isActive) accentColor else c.textHint.copy(alpha = 0.7f),
        animationSpec = tween(180), label = "search_icon_color"
    )

    val resolvedBorderColor = staticBorderColor ?: borderColor
    val resolvedBackgroundColor = staticBackgroundColor ?: c.surface
    val resolvedShadow = if (disableShadow) 0.dp else shadowElevation

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(resolvedShadow, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(resolvedBackgroundColor)
            .border(1.dp, resolvedBorderColor, RoundedCornerShape(12.dp))
            .padding(start = 10.dp, end = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (intermediateStopNumber != null) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color(0xFF8E8E93), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = intermediateStopNumber.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = searchIconColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                .onFocusChanged { if (it.isFocused) onFocused() },
            readOnly = readOnly,
            singleLine = true,
            placeholder = { Text(placeholder, fontSize = 14.sp) },
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            colors = fieldColors,
            interactionSource = interactionSource
        )
        if (showMapPicker) {
            IconButton(onClick = onMapPickerClick, modifier = Modifier.size(40.dp)) {
                if (cachedMiniMap != null) {
                    Image(
                        bitmap = cachedMiniMap,
                        contentDescription = stringResource(R.string.map_pick_on_map),
                        modifier = Modifier.size(24.dp).clip(RoundedCornerShape(5.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.ic_map_picker_route),
                        contentDescription = stringResource(R.string.map_pick_on_map),
                        modifier = Modifier.size(22.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
        if (showRemove && onRemoveClick != null) {
            IconButton(onClick = onRemoveClick, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.map_route_remove_stop),
                    tint = c.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
internal fun RouteSheetSuggestionBlock(
    results: List<AddressSearchHit>,
    loading: Boolean,
    queryLengthOk: Boolean,
    onHit: (AddressSearchHit) -> Unit
) {
    val c = LocalAppColors.current
    when {
        loading -> {
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = c.primary, strokeWidth = 2.dp)
            }
        }
        results.isNotEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.surface)
                    .border(1.dp, c.dividerGrey.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(vertical = 4.dp)
            ) {
                results.forEachIndexed { index, hit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onHit(hit) }
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(c.surfaceMuted, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = c.textSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = hit.label,
                            color = c.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (index < results.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            color = c.dividerGrey.copy(alpha = 0.35f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
        queryLengthOk -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.surfaceMuted.copy(alpha = 0.5f))
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.map_no_address_results),
                    color = c.textSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}
