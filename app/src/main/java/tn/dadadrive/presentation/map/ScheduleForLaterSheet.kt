package tn.dadadrive.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadrive.R
import tn.dadadrive.core.theme.LocalAppColors
import kotlinx.coroutines.flow.distinctUntilChanged
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

private const val SCHEDULE_MIN_LEAD_MS = 30L * 60L * 1000L
private const val SCHEDULE_MAX_DAYS_AHEAD = 30

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleForLaterSheet(
    initialEpochMs: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val c = LocalAppColors.current

    val now = remember { Calendar.getInstance() }
    val minEpochMs = remember { System.currentTimeMillis() + SCHEDULE_MIN_LEAD_MS }
    val minCalendar = remember {
        Calendar.getInstance().apply {
            timeInMillis = minEpochMs
            // round up minutes to next 5
            val m = get(Calendar.MINUTE)
            val rounded = ((m + 4) / 5) * 5
            if (rounded >= 60) {
                set(Calendar.MINUTE, 0)
                add(Calendar.HOUR_OF_DAY, 1)
            } else {
                set(Calendar.MINUTE, rounded)
            }
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    val dayItems = remember {
        val list = mutableListOf<DayItem>()
        val cal = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayFormat = SimpleDateFormat("EEE MMM d", Locale.getDefault())
        for (i in 0..SCHEDULE_MAX_DAYS_AHEAD) {
            val label = when (i) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> todayFormat.format(cal.time)
            }
            list.add(DayItem(label = label, dayOffsetFromToday = i, calendarSnapshot = (cal.clone() as Calendar)))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        list
    }

    val hours12 = remember { (1..12).toList() }
    val minutes5 = remember { (0..55 step 5).toList() }
    val ampm = remember { listOf("AM", "PM") }

    val initial = remember(initialEpochMs) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = (initialEpochMs ?: minCalendar.timeInMillis).coerceAtLeast(minCalendar.timeInMillis)
        }
        val day0 = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val dayOffset = (((cal.timeInMillis - day0.timeInMillis) / (24L * 60L * 60L * 1000L)).toInt()).coerceIn(0, SCHEDULE_MAX_DAYS_AHEAD)
        val h24 = cal.get(Calendar.HOUR_OF_DAY)
        val isPm = h24 >= 12
        val h12 = ((h24 + 11) % 12) + 1
        val m = ((cal.get(Calendar.MINUTE)) / 5) * 5
        Triple(dayOffset, Triple(h12, m, if (isPm) 1 else 0), Unit)
    }

    var dayIdx by remember { mutableStateOf(initial.first) }
    var hourIdx by remember { mutableStateOf(hours12.indexOf(initial.second.first).coerceAtLeast(0)) }
    var minuteIdx by remember { mutableStateOf(minutes5.indexOf(initial.second.second).coerceAtLeast(0)) }
    var ampmIdx by remember { mutableStateOf(initial.second.third) }

    val selectedEpochMs by remember(dayIdx, hourIdx, minuteIdx, ampmIdx) {
        derivedStateOf {
            val base = dayItems[dayIdx].calendarSnapshot.clone() as Calendar
            var h12 = hours12[hourIdx]
            val isPm = ampmIdx == 1
            var h24 = when {
                isPm && h12 == 12 -> 12
                isPm -> h12 + 12
                !isPm && h12 == 12 -> 0
                else -> h12
            }
            base.set(Calendar.HOUR_OF_DAY, h24)
            base.set(Calendar.MINUTE, minutes5[minuteIdx])
            base.set(Calendar.SECOND, 0)
            base.set(Calendar.MILLISECOND, 0)
            base.timeInMillis
        }
    }
    val isValid = selectedEpochMs >= minCalendar.timeInMillis

    val accent = Color(0xFF4FC3C8)
    val disabledAccent = accent.copy(alpha = 0.45f)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(accent.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.map_schedule_when_title),
                color = c.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.map_schedule_min_lead),
                color = c.textSecondary,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(18.dp))

            WheelRowContainer(
                height = 180.dp,
                accent = accent
            ) {
                WheelPicker(
                    items = dayItems.map { it.label },
                    selectedIndex = dayIdx,
                    onSelect = { dayIdx = it },
                    weight = 2.1f,
                    visibleCount = 5,
                )
                WheelPicker(
                    items = hours12.map { it.toString() },
                    selectedIndex = hourIdx,
                    onSelect = { hourIdx = it },
                    weight = 1f,
                    visibleCount = 5,
                )
                WheelPicker(
                    items = minutes5.map { String.format(Locale.US, "%02d", it) },
                    selectedIndex = minuteIdx,
                    onSelect = { minuteIdx = it },
                    weight = 1f,
                    visibleCount = 5,
                )
                WheelPicker(
                    items = ampm,
                    selectedIndex = ampmIdx,
                    onSelect = { ampmIdx = it },
                    weight = 1f,
                    visibleCount = 5,
                )
            }

            Spacer(Modifier.height(16.dp))

            val chipFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
            val dateFormat = remember { SimpleDateFormat("EEE MMM d", Locale.getDefault()) }
            val chipCal = remember(selectedEpochMs) {
                Calendar.getInstance().apply { timeInMillis = selectedEpochMs }
            }
            val chipText = when (dayIdx) {
                0 -> stringResource(R.string.map_schedule_today_at_format, chipFormat.format(chipCal.time))
                1 -> stringResource(R.string.map_schedule_tomorrow_at_format, chipFormat.format(chipCal.time))
                else -> stringResource(
                    R.string.map_schedule_date_at_format,
                    dateFormat.format(chipCal.time),
                    chipFormat.format(chipCal.time)
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = chipText,
                    color = accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isValid) accent else disabledAccent)
                    .clickable(enabled = isValid) { onConfirm(selectedEpochMs) }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.map_schedule_confirm_time),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.common_cancel),
                    color = c.textSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private data class DayItem(
    val label: String,
    val dayOffsetFromToday: Int,
    val calendarSnapshot: Calendar
)

@Composable
private fun WheelRowContainer(
    height: androidx.compose.ui.unit.Dp,
    accent: Color,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val c = LocalAppColors.current
    Box(modifier = Modifier.fillMaxWidth().height(height)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            content = content
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .align(Alignment.Center)
                .background(c.surfaceMuted.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    weight: Float,
    visibleCount: Int,
) {
    val c = LocalAppColors.current
    val itemHeight = 38.dp
    val halfVisible = visibleCount / 2
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val view = androidx.compose.ui.platform.LocalView.current
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current

    LaunchedEffect(listState) {
        var lastTickIdx = selectedIndex
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            layoutInfo.visibleItemsInfo.minByOrNull { info ->
                abs((info.offset + info.size / 2) - viewportCenter)
            }?.index ?: listState.firstVisibleItemIndex
        }.distinctUntilChanged().collect { idx ->
            if (idx != lastTickIdx && idx in items.indices) {
                lastTickIdx = idx
                runCatching {
                    view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                }
                runCatching {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                    } else {
                        haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    }
                }
                if (idx != selectedIndex) onSelect(idx)
            }
        }
    }

    Box(
        modifier = Modifier
            .weight(weight)
            .height(itemHeight * visibleCount),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight * halfVisible),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items.size) { index ->
                val distance = abs(index - selectedIndex)
                val alphaValue = when (distance) {
                    0 -> 1f
                    1 -> 0.45f
                    else -> 0.18f
                }
                val fontSizeSp = when (distance) {
                    0 -> 17
                    1 -> 14
                    else -> 13
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[index],
                        color = c.textPrimary,
                        fontSize = fontSizeSp.sp,
                        fontWeight = if (distance == 0) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.alpha(alphaValue)
                    )
                }
            }
        }
    }
}
