package tn.turbodrive.presentation.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbodrive.R
import kotlinx.coroutines.delay
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.theme.LocalAppColors
import tn.turbodrive.core.theme.MapColorTokens
import tn.turbodrive.presentation.components.BlackCloseIconButton

internal sealed class ActiveRouteField {
    data object Origin : ActiveRouteField()

    data class Intermediate(val index: Int) : ActiveRouteField()

    data object FinalDestination : ActiveRouteField()
}

private const val SCHEDULE_PICKUP_MIN_LEAD_MS = 30L * 60L * 1000L
private const val ROUTE_SHEET_MAX_SUGGESTIONS = 6
private val TimelineLineWidth = 2.dp
private val TimelineStartEndCircle = 12.dp
private val TimelineStopCircle = 7.dp
private val TimelineConnectorShort = 7.dp
private val TimelineConnectorMid = 9.dp

internal enum class RouteTimelineKind { Start, Stop, End }

// ─────────────────────────────────────────────────────────────────────────────
// Date/Time picker
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RouteItineraryBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable (sheetState: SheetState) -> Unit,
) {
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = false,
            confirmValueChange = { true },
        )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = LocalAppColors.current.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
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
    intermediateStops: List<IntermediateStopDraft>,
    finalDestination: String,
    onIntermediateStopChange: (index: Int, value: String) -> Unit,
    onAddIntermediateStop: () -> Unit,
    onRemoveIntermediateStop: (index: Int) -> Unit,
    onFinalDestinationChange: (String) -> Unit,
    onClose: () -> Unit,
    onOpenPickupMapPicker: () -> Unit,
    onOpenDestinationMapPicker: () -> Unit,
    onOpenIntermediateStopMapPicker: (index: Int, append: Boolean) -> Unit,
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
    sheetState: SheetState? = null,
    pickupSearchResults: List<AddressSearchHit> = emptyList(),
    pickupSearchLoading: Boolean = false,
    addressSearchResults: List<AddressSearchHit> = emptyList(),
    addressSearchLoading: Boolean = false,
    onPickupSuggestionPick: (AddressSearchHit) -> Unit = {},
    onDestinationSuggestionPick: (AddressSearchHit) -> Unit = {},
    onIntermediateSuggestionPick: (Int, AddressSearchHit) -> Unit = { _, _ -> },
) {
    val c = LocalAppColors.current
    val maxIntermediateStops = 4

    val mutedText = MaterialTheme.colorScheme.onSurfaceVariant
    val fieldColors =
        TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = c.textPrimary,
            unfocusedTextColor = c.textPrimary,
            cursorColor = c.primary,
            focusedPlaceholderColor = c.textSubtle,
            unfocusedPlaceholderColor = c.textSubtle,
        )
    val intermediateFieldColors =
        TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = mutedText,
            unfocusedTextColor = mutedText,
            cursorColor = c.primary,
            focusedPlaceholderColor = mutedText.copy(alpha = 0.75f),
            unfocusedPlaceholderColor = mutedText.copy(alpha = 0.75f),
        )
    val originInteraction = remember { MutableInteractionSource() }
    val intermediateInteractions =
        remember(intermediateStops.size) {
            List(intermediateStops.size) { MutableInteractionSource() }
        }
    val finalDestinationInteraction = remember { MutableInteractionSource() }
    val originFocused by originInteraction.collectIsFocusedAsState()
    val finalDestinationFocused by finalDestinationInteraction.collectIsFocusedAsState()
    val originFocusRequester = remember { FocusRequester() }
    val finalDestinationFocusRequester = remember { FocusRequester() }
    val intermediateFocusRequesters = remember { mutableStateListOf<FocusRequester>() }
    LaunchedEffect(intermediateStops.size) {
        while (intermediateFocusRequesters.size < intermediateStops.size) {
            intermediateFocusRequesters.add(FocusRequester())
        }
        while (intermediateFocusRequesters.size > intermediateStops.size) {
            intermediateFocusRequesters.removeAt(intermediateFocusRequesters.lastIndex)
        }
    }
    val intermediateFocusedStates = intermediateInteractions.map { it.collectIsFocusedAsState().value }
    val focusedIntermediateIndex =
        remember(intermediateFocusedStates) {
            intermediateFocusedStates.indexOfFirst { it }
        }
    val anyIntermediateFocused = focusedIntermediateIndex >= 0
    var pendingNewIntermediateFocus by remember { mutableStateOf(false) }
    var activeField by remember { mutableStateOf<ActiveRouteField>(ActiveRouteField.Origin) }

    LaunchedEffect(originFocused, focusedIntermediateIndex, finalDestinationFocused) {
        when {
            originFocused -> activeField = ActiveRouteField.Origin
            anyIntermediateFocused -> activeField = ActiveRouteField.Intermediate(focusedIntermediateIndex)
            finalDestinationFocused -> activeField = ActiveRouteField.FinalDestination
        }
    }

    val miniMapIcon = rememberRouteSheetMiniMapBitmap()

    LaunchedEffect(intermediateStops.size, pendingNewIntermediateFocus) {
        if (pendingNewIntermediateFocus && intermediateStops.isNotEmpty()) {
            val idx = intermediateStops.lastIndex
            activeField = ActiveRouteField.Intermediate(idx)
            delay(48)
            intermediateFocusRequesters.getOrNull(idx)?.requestFocus()
            pendingNewIntermediateFocus = false
        }
    }

    var showScheduleSheet by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            BlackCloseIconButton(onClick = onClose, buttonSize = 28.dp, iconSize = 15.dp)
        }

        Spacer(Modifier.height(6.dp))

        // ── For me / For other toggle ──────────────────────────────────────
        ForMeForOtherSegmented(
            forMe = forMe,
            onForMeChange = { newForMe -> if (newForMe != forMe) onForMeToggle() },
        )

        if (!forMe) {
            Spacer(Modifier.height(10.dp))
            PassengerInfoFields(
                name = passengerName,
                onNameChange = onPassengerNameChange,
                phone = passengerPhone,
                onPhoneChange = onPassengerPhoneChange,
                fieldColors = fieldColors,
            )
        }

        Spacer(Modifier.height(10.dp))

        // ── Timeline: départ (grand cercle noir) → arrêts (petits jaunes) → arrivée (grand rouge) ──
        RouteTimelineLegRow(
            kind = RouteTimelineKind.Start,
            connectorUp = false,
            connectorDown = true,
        ) {
            if (originFocused) {
                RouteSheetFieldBackBar(onBack = { focusManager.clearFocus() })
                Spacer(Modifier.height(2.dp))
            }
            RouteSmallLabel(stringResource(R.string.map_route_from_label))
            Spacer(Modifier.height(4.dp))
            SwiftStyleRouteField(
                dotColor = c.primary,
                dotFilled = true,
                value = originValue,
                onValueChange = onOriginChange,
                placeholder = stringResource(R.string.map_route_current_location),
                isActive = originFocused,
                focusRequester = originFocusRequester,
                interactionSource = originInteraction,
                fieldColors = fieldColors,
                imeAction = ImeAction.Search,
                showClear = originValue.isNotBlank(),
                onClear = { onOriginChange("") },
                onMapPickerClick = onOpenPickupMapPicker,
                cachedMiniMap = miniMapIcon,
                onFocused = { activeField = ActiveRouteField.Origin },
                onClick = {
                    activeField = ActiveRouteField.Origin
                    originFocusRequester.requestFocus()
                },
                showLeadingDot = false,
            )
            if (originFocused) {
                Spacer(Modifier.height(6.dp))
                RouteSheetSuggestionBlock(
                    results = pickupSearchResults.take(ROUTE_SHEET_MAX_SUGGESTIONS),
                    loading = pickupSearchLoading,
                    queryLengthOk = originValue.trim().length >= 3,
                    onHit = onPickupSuggestionPick,
                )
            }
        }

        intermediateStops.forEachIndexed { index, draft ->
            RouteTimelineLegRow(
                kind = RouteTimelineKind.Stop,
                connectorUp = true,
                connectorDown = true,
            ) {
                SwiftStyleRouteField(
                    dotColor = Color(STOP_PIN_YELLOW_ARGB),
                    dotFilled = true,
                    value = draft.label,
                    onValueChange = { onIntermediateStopChange(index, it) },
                    placeholder = stringResource(R.string.map_destination_placeholder),
                    isActive = intermediateFocusedStates.getOrNull(index) == true,
                    focusRequester = intermediateFocusRequesters.getOrNull(index),
                    interactionSource = intermediateInteractions[index],
                    fieldColors = intermediateFieldColors,
                    imeAction = ImeAction.Search,
                    showClear = draft.label.isNotBlank(),
                    onClear = { onIntermediateStopChange(index, "") },
                    showMapPicker = false,
                    onMapPickerClick = { onOpenIntermediateStopMapPicker(index, false) },
                    cachedMiniMap = miniMapIcon,
                    onFocused = { activeField = ActiveRouteField.Intermediate(index) },
                    onRemoveClick = { onRemoveIntermediateStop(index) },
                    onClick = {
                        activeField = ActiveRouteField.Intermediate(index)
                        intermediateFocusRequesters.getOrNull(index)?.requestFocus()
                    },
                    showLeadingDot = false,
                )
                if (intermediateFocusedStates.getOrNull(index) == true) {
                    Spacer(Modifier.height(6.dp))
                    RouteSheetSuggestionBlock(
                        results = addressSearchResults.take(ROUTE_SHEET_MAX_SUGGESTIONS),
                        loading = addressSearchLoading,
                        queryLengthOk = draft.label.trim().length >= 3,
                        onHit = { onIntermediateSuggestionPick(index, it) },
                    )
                }
                if (draft.validity == IntermediateStopValidity.OffRoute) {
                    Spacer(Modifier.height(4.dp))
                    StopValidityHint(
                        text = stringResource(R.string.map_stop_off_route_warning),
                        color = c.error,
                    )
                } else if (draft.validity == IntermediateStopValidity.OnRoute) {
                    Spacer(Modifier.height(4.dp))
                    StopValidityHint(
                        text = stringResource(R.string.map_stop_on_route_ok),
                        color = c.accent,
                    )
                }
            }
        }

        RouteTimelineLegRow(
            kind = RouteTimelineKind.End,
            connectorUp = true,
            connectorDown = false,
        ) {
            if (finalDestinationFocused) {
                RouteSheetFieldBackBar(onBack = { focusManager.clearFocus() })
                Spacer(Modifier.height(2.dp))
            }
            RouteSmallLabel(stringResource(R.string.map_route_to_label))
            Spacer(Modifier.height(4.dp))
            SwiftStyleRouteField(
                dotColor = c.error,
                dotFilled = true,
                value = finalDestination,
                onValueChange = onFinalDestinationChange,
                placeholder = stringResource(R.string.map_route_to_placeholder),
                isActive = finalDestinationFocused,
                focusRequester = finalDestinationFocusRequester,
                interactionSource = finalDestinationInteraction,
                fieldColors = fieldColors,
                imeAction = ImeAction.Search,
                showClear = finalDestination.isNotBlank(),
                onClear = { onFinalDestinationChange("") },
                onMapPickerClick = onOpenDestinationMapPicker,
                cachedMiniMap = miniMapIcon,
                onFocused = { activeField = ActiveRouteField.FinalDestination },
                onClick = {
                    activeField = ActiveRouteField.FinalDestination
                    finalDestinationFocusRequester.requestFocus()
                },
                showLeadingDot = false,
            )
            if (finalDestinationFocused) {
                Spacer(Modifier.height(6.dp))
                RouteSheetSuggestionBlock(
                    results = addressSearchResults.take(ROUTE_SHEET_MAX_SUGGESTIONS),
                    loading = addressSearchLoading,
                    queryLengthOk = finalDestination.trim().length >= 3,
                    onHit = onDestinationSuggestionPick,
                )
            }
        }

        if (intermediateStops.size < maxIntermediateStops) {
            Spacer(Modifier.height(8.dp))
            AddStopButton(onClick = {
                pendingNewIntermediateFocus = true
                onAddIntermediateStop()
            })
        }

        Spacer(Modifier.height(10.dp))

        // ── OR divider ─────────────────────────────────────────────────────
        OrDivider()

        Spacer(Modifier.height(10.dp))

        // ── Schedule for later button ──────────────────────────────────────
        ScheduleForLaterButton(
            scheduledAtEpochMs = scheduledAtEpochMs,
            onClick = { showScheduleSheet = true },
        )

        Spacer(Modifier.height(6.dp))
    }

    if (showScheduleSheet) {
        ScheduleForLaterSheet(
            initialEpochMs = scheduledAtEpochMs,
            onDismiss = { showScheduleSheet = false },
            onConfirm = { epoch ->
                onScheduledAtChosen(epoch)
                showScheduleSheet = false
            },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Swift-style route inputs (pill fields with colored dot + clear + map picker)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ForMeForOtherSegmented(
    forMe: Boolean,
    onForMeChange: (Boolean) -> Unit,
    darkItinerary: Boolean = false,
) {
    val c = LocalAppColors.current
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (darkItinerary) {
                        Color.White.copy(alpha = 0.08f)
                    } else {
                        c.surfaceAlt.copy(alpha = 0.55f)
                    },
                )
                .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SegmentItem(
            label = stringResource(R.string.map_for_me),
            selected = forMe,
            onClick = { onForMeChange(true) },
            modifier = Modifier.weight(1f),
            darkItinerary = darkItinerary,
        )
        SegmentItem(
            label = stringResource(R.string.map_for_other),
            selected = !forMe,
            onClick = { onForMeChange(false) },
            modifier = Modifier.weight(1f),
            darkItinerary = darkItinerary,
        )
    }
}

@Composable
private fun SegmentItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    darkItinerary: Boolean = false,
) {
    val c = LocalAppColors.current
    val bgColor =
        when {
            darkItinerary && selected -> Color.White
            darkItinerary -> Color.Transparent
            selected -> Color.Black
            else -> Color.Transparent
        }
    val textColor =
        when {
            darkItinerary && selected -> Color.Black
            darkItinerary -> Color.White.copy(alpha = 0.72f)
            selected -> Color.White
            else -> c.textSecondary
        }
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor)
                .clickable(onClick = onClick)
                .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

@Composable
private fun StopValidityHint(
    text: String,
    color: Color,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PassengerInfoFields(
    name: String,
    onNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors,
    darkItinerary: Boolean = false,
) {
    val c = LocalAppColors.current
    val nameInteraction = remember { MutableInteractionSource() }
    val phoneInteraction = remember { MutableInteractionSource() }
    val nameFocused by nameInteraction.collectIsFocusedAsState()
    val phoneFocused by phoneInteraction.collectIsFocusedAsState()

    val inactiveBorder = if (darkItinerary) Color.White.copy(alpha = 0.18f) else c.dividerGrey.copy(alpha = 0.50f)
    val activeBorder = if (darkItinerary) Color.White else Color.Black

    val nameBorderColor = if (nameFocused) activeBorder else inactiveBorder
    val nameBorderWidth = if (nameFocused) 1.6.dp else 1.dp
    val phoneBorderColor = if (phoneFocused) activeBorder else inactiveBorder
    val phoneBorderWidth = if (phoneFocused) 1.6.dp else 1.dp

    val rowBg = if (darkItinerary) MapColorTokens.darkPanelSurface else c.surface
    val iconTint = if (darkItinerary) Color.White.copy(alpha = 0.72f) else c.textSecondary

    val corner = 16.dp
    val iconSize = 20.dp
    val innerH = 16.dp
    val innerV = 8.dp

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(corner))
                    .background(rowBg)
                    .border(nameBorderWidth, nameBorderColor, RoundedCornerShape(corner))
                    .padding(horizontal = innerH, vertical = innerV),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                painter = painterResource(AppIcon.user),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(iconSize),
            )
            TextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                interactionSource = nameInteraction,
                placeholder = {
                    Text(stringResource(R.string.map_passenger_name_placeholder_for_other), fontSize = 15.sp)
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = fieldColors,
            )
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(corner))
                    .background(rowBg)
                    .border(phoneBorderWidth, phoneBorderColor, RoundedCornerShape(corner))
                    .padding(horizontal = innerH, vertical = innerV),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                painter = painterResource(AppIcon.phone),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(iconSize),
            )
            TextField(
                value = phone,
                onValueChange = onPhoneChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                interactionSource = phoneInteraction,
                placeholder = {
                    Text(stringResource(R.string.map_passenger_phone_placeholder_for_other), fontSize = 15.sp)
                },
                keyboardOptions =
                    KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
                    ),
                colors = fieldColors,
            )
        }
    }
}

@Composable
internal fun RouteTimelineLegRow(
    kind: RouteTimelineKind,
    connectorUp: Boolean,
    connectorDown: Boolean,
    content: @Composable () -> Unit,
) {
    val c = LocalAppColors.current
    val lineColor = c.dividerGrey.copy(alpha = 0.55f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(26.dp),
        ) {
            if (connectorUp) {
                Box(
                    Modifier
                        .width(TimelineLineWidth)
                        .height(TimelineConnectorShort)
                        .background(lineColor),
                )
            }
            val diameter =
                when (kind) {
                    RouteTimelineKind.Start, RouteTimelineKind.End -> TimelineStartEndCircle
                    RouteTimelineKind.Stop -> TimelineStopCircle
                }
            val dotColor =
                when (kind) {
                    RouteTimelineKind.Start -> Color.Black
                    RouteTimelineKind.Stop -> Color(STOP_PIN_YELLOW_ARGB)
                    RouteTimelineKind.End -> c.error
                }
            Box(
                modifier =
                    Modifier
                        .size(diameter)
                        .background(dotColor, CircleShape),
            )
            if (connectorDown) {
                Box(
                    Modifier
                        .width(TimelineLineWidth)
                        .height(TimelineConnectorMid)
                        .background(lineColor),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

@Composable
private fun RouteSmallLabel(
    text: String,
    darkItinerary: Boolean = false,
) {
    val c = LocalAppColors.current
    val labelColor = if (darkItinerary) Color.White.copy(alpha = 0.68f) else c.textSecondary
    val lineColor =
        if (darkItinerary) Color.White.copy(alpha = 0.22f) else c.dividerGrey.copy(alpha = 0.35f)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            color = labelColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.width(6.dp))
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(lineColor),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwiftStyleRouteField(
    dotColor: Color,
    dotFilled: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isActive: Boolean,
    focusRequester: FocusRequester?,
    interactionSource: MutableInteractionSource,
    fieldColors: androidx.compose.material3.TextFieldColors,
    imeAction: ImeAction,
    showClear: Boolean,
    onClear: () -> Unit,
    showMapPicker: Boolean = true,
    onMapPickerClick: () -> Unit,
    cachedMiniMap: androidx.compose.ui.graphics.ImageBitmap?,
    onFocused: () -> Unit,
    onRemoveClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    showLeadingDot: Boolean = true,
    darkItineraryStyle: Boolean = false,
) {
    val c = LocalAppColors.current
    val focusManager = LocalFocusManager.current
    val borderColor =
        when {
            darkItineraryStyle && isActive -> Color.White
            darkItineraryStyle -> Color.White.copy(alpha = 0.22f)
            isActive -> Color.Black
            else -> c.dividerGrey.copy(alpha = 0.50f)
        }
    val borderWidth = if (isActive) 1.6.dp else 1.dp
    val pillSurface = if (darkItineraryStyle) MapColorTokens.darkPanelSurface else c.surface
    val pillCorner = 16.dp
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(pillCorner))
                .background(pillSurface)
                .border(borderWidth, borderColor, RoundedCornerShape(pillCorner))
                .clickable(onClick = onClick)
                .padding(start = 15.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (showLeadingDot) {
            Box(
                modifier = Modifier.size(15.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (dotFilled) {
                    Box(
                        modifier =
                            Modifier
                                .size(15.dp)
                                .background(dotColor.copy(alpha = 0.20f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(9.dp)
                                    .background(dotColor, CircleShape),
                        )
                    }
                } else {
                    Box(
                        modifier =
                            Modifier
                                .size(11.dp)
                                .border(2.dp, dotColor, CircleShape),
                    )
                }
            }
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier =
                Modifier
                    .weight(1f)
                    .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                    .onFocusChanged { if (it.isFocused) onFocused() },
            singleLine = true,
            placeholder = { Text(placeholder, fontSize = 14.sp) },
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            keyboardActions =
                KeyboardActions(
                    onSearch = { focusManager.clearFocus() },
                    onDone = { focusManager.clearFocus() },
                    onGo = { focusManager.clearFocus() },
                ),
            colors = fieldColors,
            interactionSource = interactionSource,
        )
        if (showClear) {
            BlackCloseIconButton(
                onClick = onClear,
                buttonSize = 28.dp,
                iconSize = 14.dp,
            )
        }
        if (onRemoveClick != null) {
            BlackCloseIconButton(
                onClick = onRemoveClick,
                buttonSize = 28.dp,
                iconSize = 14.dp,
                contentDescription = stringResource(R.string.map_route_remove_stop),
            )
        }
        if (showMapPicker) {
            IconButton(onClick = onMapPickerClick, modifier = Modifier.size(40.dp)) {
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (darkItineraryStyle) {
                                    Color.White.copy(alpha = 0.14f)
                                } else {
                                    c.primary.copy(alpha = 0.14f)
                                },
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (cachedMiniMap != null) {
                        Image(
                            bitmap = cachedMiniMap,
                            contentDescription = stringResource(R.string.map_pick_on_map),
                            modifier = Modifier.size(22.dp).clip(RoundedCornerShape(5.dp)),
                            contentScale = ContentScale.Fit,
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.ic_map_picker_route),
                            contentDescription = stringResource(R.string.map_pick_on_map),
                            modifier = Modifier.size(20.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteSheetFieldBackBar(onBack: () -> Unit) {
    val c = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                painter = painterResource(AppIcon.arrowLeft),
                contentDescription = stringResource(R.string.cd_back),
                tint = c.textPrimary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun AddStopButton(onClick: () -> Unit) {
    val c = LocalAppColors.current
    val bg = Color(STOP_PIN_YELLOW_ARGB)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(bg)
                .clickable(onClick = onClick)
                .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            painter = painterResource(AppIcon.plus),
            contentDescription = null,
            tint = c.textPrimary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(R.string.map_add_stop),
            color = c.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun OrDivider(darkItinerary: Boolean = false) {
    val c = LocalAppColors.current
    val line = if (darkItinerary) Color.White.copy(alpha = 0.22f) else c.dividerGrey.copy(alpha = 0.40f)
    val textCol = if (darkItinerary) Color.White.copy(alpha = 0.65f) else c.textSecondary
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(line),
        )
        Text(
            text = stringResource(R.string.map_or_divider),
            color = textCol,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(line),
        )
    }
}

@Composable
private fun ScheduleForLaterButton(
    scheduledAtEpochMs: Long?,
    onClick: () -> Unit,
    darkItinerary: Boolean = false,
) {
    val c = LocalAppColors.current
    val bg = if (darkItinerary) MapColorTokens.darkPanelSurface else c.surface
    val borderCol = if (darkItinerary) Color.White.copy(alpha = 0.20f) else c.dividerGrey.copy(alpha = 0.45f)
    val titleCol = if (darkItinerary) Color.White else c.textPrimary
    val subtitleCol = if (darkItinerary) Color.White.copy(alpha = 0.68f) else c.textSecondary
    val iconCircle = if (darkItinerary) Color.White.copy(alpha = 0.10f) else c.surfaceAlt.copy(alpha = 0.55f)
    val iconTint = if (darkItinerary) Color.White.copy(alpha = 0.72f) else c.textSecondary
    val chevronTint = if (darkItinerary) Color.White.copy(alpha = 0.45f) else c.textSubtle
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(bg)
                .border(1.dp, borderCol, RoundedCornerShape(14.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(34.dp)
                    .background(iconCircle, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(AppIcon.clock),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.map_schedule_for_later_title),
                color = titleCol,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            val subtitle =
                if (scheduledAtEpochMs != null) {
                    val fmt = java.text.SimpleDateFormat("EEE MMM d, HH:mm", java.util.Locale.getDefault())
                    fmt.format(java.util.Date(scheduledAtEpochMs))
                } else {
                    stringResource(R.string.map_schedule_for_later_subtitle)
                }
            Text(
                text = subtitle,
                color = subtitleCol,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
            )
        }
        Icon(
            painter = painterResource(AppIcon.chevronRight),
            contentDescription = null,
            tint = chevronTint,
            modifier = Modifier.size(22.dp),
        )
    }
}
