package tn.turbodrive.presentation.map

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.search.Place
import com.turbodrive.R
import tn.turbodrive.core.theme.LocalAppColors
import kotlin.math.roundToInt

@Composable
private fun localizedCategoryLabel(category: PoiCategory): String =
    when (category) {
        PoiCategory.HOSPITAL -> stringResource(R.string.poi_category_hospital)
        PoiCategory.CLINIC -> stringResource(R.string.poi_category_clinic)
        PoiCategory.MOSQUE -> stringResource(R.string.poi_category_mosque)
        PoiCategory.SCHOOL -> stringResource(R.string.poi_category_school)
        PoiCategory.BANK -> stringResource(R.string.poi_category_bank)
        PoiCategory.KIOSK -> stringResource(R.string.poi_category_kiosk)
        PoiCategory.GOVERNMENT -> stringResource(R.string.poi_category_government)
        PoiCategory.CUSTOM_LOCATION -> category.labelFr
    }

@Composable
internal fun PoiCategoryHorizontalBar(
    selectedCategory: PoiCategory?,
    onCategoryClick: (PoiCategory) -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val c = LocalAppColors.current
    val categories = remember { PoiCategory.entries.filter { it != PoiCategory.CUSTOM_LOCATION } }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (leading != null) {
            Row(
                modifier = Modifier.widthIn(max = 52.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leading()
            }
        }
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(categories, key = { it.name }) { cat ->
                val selected = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (selected) c.textPrimary else Color.White.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, c.dividerGrey.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable { onCategoryClick(cat) },
                ) {
                    Text(
                        text = localizedCategoryLabel(cat),
                        color = if (selected) Color.White else c.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
            }
        }
        trailing?.invoke()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PoiBottomSheet(
    place: Place,
    category: PoiCategory,
    preferredField: PoiSearchField?,
    selectionTarget: PoiSelectionTarget?,
    userLocation: GeoCoordinates?,
    onSetPickup: (Place) -> Unit,
    onSetDropoff: (Place) -> Unit,
    onSetIntermediateStop: (Place) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val image =
        remember {
            runCatching {
                context.assets.open("home.png").use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
            }.getOrNull()
        }
    val poiGeo = place.geoCoordinates
    val distanceText =
        if (userLocation != null && poiGeo != null) {
            val dist = userLocation.distanceTo(poiGeo)
            if (dist < 1000.0) "${dist.roundToInt()} m" else String.format("%.1f km", dist / 1000.0)
        } else {
            ""
        }
    val isIntermediate = selectionTarget is PoiSelectionTarget.IntermediateStop
    val isPickupFlow =
        when (selectionTarget) {
            is PoiSelectionTarget.Pickup -> true
            is PoiSelectionTarget.Destination -> false
            is PoiSelectionTarget.IntermediateStop -> false
            null -> preferredField == PoiSearchField.PICKUP
        }
    val primaryLabel =
        when {
            isIntermediate -> stringResource(R.string.poi_confirm_as_stop)
            isPickupFlow -> stringResource(R.string.poi_confirm_as_pickup)
            else -> stringResource(R.string.poi_confirm_as_dropoff)
        }
    val chooseOtherLabel = stringResource(R.string.poi_choose_the_other)
    val c = LocalAppColors.current
    val contextTitle =
        when {
            isIntermediate -> primaryLabel
            isPickupFlow -> stringResource(R.string.poi_confirm_as_pickup)
            else -> stringResource(R.string.poi_confirm_as_dropoff)
        }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (image != null) {
                    Image(bitmap = image, contentDescription = null, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.size(10.dp))
                }
                Column {
                    Text(
                        text = localizedCategoryLabel(category),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        text = distanceText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = contextTitle,
                style = MaterialTheme.typography.labelLarge,
                color = c.textSecondary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = place.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = place.address?.addressText.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(16.dp))
            if (isIntermediate) {
                Button(
                    onClick = {
                        onSetIntermediateStop(place)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                ) { Text(primaryLabel) }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            if (isPickupFlow) onSetPickup(place) else onSetDropoff(place)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                    ) { Text(primaryLabel) }
                    OutlinedButton(
                        onClick = {
                            if (isPickupFlow) onSetDropoff(place) else onSetPickup(place)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(999.dp),
                    ) { Text(chooseOtherLabel) }
                }
            }
        }
    }
}
