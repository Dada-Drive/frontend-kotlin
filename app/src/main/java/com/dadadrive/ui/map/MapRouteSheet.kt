package com.dadadrive.ui.map

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadrive.R
import com.dadadrive.ui.theme.LocalAppColors
import kotlinx.coroutines.launch
import java.util.Calendar

internal sealed class ActiveRouteField {
    data object Origin : ActiveRouteField()
    data class Intermediate(val index: Int) : ActiveRouteField()
    data object FinalDestination : ActiveRouteField()
}
private const val SCHEDULE_PICKUP_MIN_LEAD_MS = 30L * 60L * 1000L

// ─────────────────────────────────────────────────────────────────────────────
// Date/Time picker
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RouteItineraryBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable (sheetState: SheetState) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
        confirmValueChange = { true }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = LocalAppColors.current.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        content(sheetState)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Fluid animated text field row
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RouteItinerarySheetContent(
    originValue: String,
    onOriginChange: (String) -> Unit,
    intermediateStops: List<String>,
    finalDestination: String,
    onIntermediateStopChange: (index: Int, value: String) -> Unit,
    onAddIntermediateStop: () -> Unit,
    onRemoveIntermediateStop: (index: Int) -> Unit,
    onFinalDestinationChange: (String) -> Unit,
    pickupResults: List<AddressSearchHit>,
    pickupSearchLoading: Boolean,
    destinationResults: List<AddressSearchHit>,
    destinationSearchLoading: Boolean,
    onPickupHit: (AddressSearchHit) -> Unit,
    onIntermediateStopHit: (index: Int, hit: AddressSearchHit) -> Unit,
    onFinalDestinationHit: (AddressSearchHit) -> Unit,
    onClose: () -> Unit,
    onOpenPickupMapPicker: () -> Unit,
    onOpenDestinationMapPicker: () -> Unit,
    pickupNow: Boolean,
    onPickupNowToggle: () -> Unit,
    forMe: Boolean,
    onForMeToggle: () -> Unit,
    scheduledAtEpochMs: Long?,
    onScheduledAtChosen: (Long) -> Unit,
    passengerName: String,
    onPassengerNameChange: (String) -> Unit,
    passengerPhone: String,
    onPassengerPhoneChange: (String) -> Unit,
    sheetState: SheetState? = null
) {
    val c = LocalAppColors.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val maxIntermediateStops = 4

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
    val intermediateFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        cursorColor = c.primary,
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
    )
    val originFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        focusedTextColor = c.textSecondary,
        unfocusedTextColor = c.textSecondary,
        cursorColor = c.primary,
        focusedPlaceholderColor = c.textHint,
        unfocusedPlaceholderColor = c.textHint
    )

    val originInteraction = remember { MutableInteractionSource() }
    val intermediateInteractions = remember(intermediateStops.size) {
        List(intermediateStops.size) { MutableInteractionSource() }
    }
    val finalDestinationInteraction = remember { MutableInteractionSource() }
    val originFocused by originInteraction.collectIsFocusedAsState()
    val finalDestinationFocused by finalDestinationInteraction.collectIsFocusedAsState()
    val finalDestinationFocusRequester = remember { FocusRequester() }
    val intermediateFocusRequesters = remember { mutableStateListOf<FocusRequester>() }
    LaunchedEffect(intermediateStops.size) {
        while (intermediateFocusRequesters.size < intermediateStops.size) {
            intermediateFocusRequesters.add(FocusRequester())
        }
        while (intermediateFocusRequesters.size > intermediateStops.size) {
            intermediateFocusRequesters.removeLast()
        }
    }
    val intermediateFocusedStates = intermediateInteractions.map { it.collectIsFocusedAsState().value }
    var pendingNewIntermediateFocus by remember { mutableStateOf(false) }
    var activeField by remember { mutableStateOf<ActiveRouteField>(ActiveRouteField.Origin) }

    if (originFocused) {
        activeField = ActiveRouteField.Origin
    }
    intermediateFocusedStates.indexOfFirst { it }.takeIf { it >= 0 }?.let { idx ->
        activeField = ActiveRouteField.Intermediate(idx)
    }
    if (finalDestinationFocused) {
        activeField = ActiveRouteField.FinalDestination
    }

    val miniMapIcon = rememberRouteSheetMiniMapBitmap()

    // Auto-expand sheet when keyboard opens
    val anyFieldFocused = originFocused || finalDestinationFocused || intermediateFocusedStates.any { it }
    LaunchedEffect(anyFieldFocused) {
        if (anyFieldFocused && sheetState != null) {
            scope.launch { sheetState.expand() }
        }
    }
    LaunchedEffect(intermediateStops.size, pendingNewIntermediateFocus) {
        if (pendingNewIntermediateFocus && intermediateStops.isNotEmpty()) {
            intermediateFocusRequesters.lastOrNull()?.requestFocus()
            pendingNewIntermediateFocus = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .statusBarsPadding()
            .padding(top = 8.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        // ── Header ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.map_enter_route),
                color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClose) {
                Surface(shape = CircleShape, color = c.surfaceMuted, modifier = Modifier.size(36.dp)) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = c.textHint, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Route input card ───────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = c.surfaceMuted.copy(alpha = 0.45f),
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // ── LEFT: Connector icon ───────────────────────────────────
                RouteConnectorIcon(
                    intermediateCount = intermediateStops.size,
                    activeField = activeField
                )

                // ── CENTER: Fields ─────────────────────────────────────────
                Column(modifier = Modifier.weight(1f)) {

                    // Origin field
                    RouteTextField(
                        value = stringResource(R.string.map_route_origin_placeholder),
                        onValueChange = {},
                        placeholder = stringResource(R.string.map_route_origin_placeholder),
                        isActive = activeField is ActiveRouteField.Origin,
                        isOrigin = true,
                        readOnly = true,
                        interactionSource = originInteraction,
                        fieldColors = originFieldColors,
                        imeAction = ImeAction.Next,
                        onMapPickerClick = onOpenPickupMapPicker,
                        cachedMiniMap = miniMapIcon,
                        onFocused = {
                            activeField = ActiveRouteField.Origin
                        },
                        modifier = Modifier.clickable {
                            activeField = ActiveRouteField.Origin
                            focusManager.clearFocus()
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(c.dividerGrey.copy(alpha = 0.30f))
                    )

                    intermediateStops.forEachIndexed { index, stopValue ->
                        Spacer(Modifier.height(8.dp))
                        RouteTextField(
                            value = stopValue,
                            onValueChange = { onIntermediateStopChange(index, it) },
                            placeholder = stringResource(R.string.map_destination_placeholder),
                            isActive = activeField == ActiveRouteField.Intermediate(index),
                            isOrigin = false,
                            focusRequester = intermediateFocusRequesters.getOrNull(index),
                            interactionSource = intermediateInteractions[index],
                            fieldColors = intermediateFieldColors,
                            imeAction = ImeAction.Search,
                            onMapPickerClick = {},
                            cachedMiniMap = miniMapIcon,
                            onFocused = {
                                activeField = ActiveRouteField.Intermediate(index)
                            },
                            showRemove = true,
                            onRemoveClick = { onRemoveIntermediateStop(index) },
                            intermediateStopNumber = index + 1,
                            showMapPicker = false,
                            staticBorderColor = Color(0xFF3A3A3C),
                            staticBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            staticTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disableShadow = true,
                            modifier = Modifier.clickable {
                                activeField = ActiveRouteField.Intermediate(index)
                                intermediateFocusRequesters.getOrNull(index)?.requestFocus()
                            }
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    RouteTextField(
                        value = finalDestination,
                        onValueChange = onFinalDestinationChange,
                        placeholder = stringResource(R.string.map_destination_placeholder),
                        isActive = activeField is ActiveRouteField.FinalDestination,
                        isOrigin = false,
                        focusRequester = finalDestinationFocusRequester,
                        interactionSource = finalDestinationInteraction,
                        fieldColors = fieldColors,
                        imeAction = ImeAction.Search,
                        onMapPickerClick = onOpenDestinationMapPicker,
                        cachedMiniMap = miniMapIcon,
                        onFocused = {
                            activeField = ActiveRouteField.FinalDestination
                        },
                        modifier = Modifier.clickable {
                            activeField = ActiveRouteField.FinalDestination
                            finalDestinationFocusRequester.requestFocus()
                        }
                    )

                    if (finalDestination.isNotBlank() && intermediateStops.size < maxIntermediateStops) {
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    pendingNewIntermediateFocus = true
                                    onAddIntermediateStop()
                                },
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = c.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Ajouter un arrêt",
                                color = c.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Suggestion list ────────────────────────────────────────────────
        val activeDestinationText = when (val field = activeField) {
            is ActiveRouteField.Intermediate -> intermediateStops.getOrElse(field.index) { "" }
            is ActiveRouteField.FinalDestination -> finalDestination
            is ActiveRouteField.Origin -> ""
        }
        RouteSheetSuggestionBlock(
            results = destinationResults,
            loading = destinationSearchLoading,
            queryLengthOk = activeDestinationText.trim().length >= 3,
            onHit = { hit ->
                when (val field = activeField) {
                    is ActiveRouteField.Intermediate -> onIntermediateStopHit(field.index, hit)
                    is ActiveRouteField.FinalDestination -> onFinalDestinationHit(hit)
                    is ActiveRouteField.Origin -> Unit
                }
                focusManager.clearFocus()
            }
        )

        Spacer(Modifier.weight(1f))
    }
}

