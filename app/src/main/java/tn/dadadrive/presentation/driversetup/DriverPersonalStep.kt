package tn.dadadrive.presentation.driversetup

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadrive.R

@Composable
internal fun DriverPersonalStep(
    cinFrontBmp: Bitmap?,
    cinBackBmp: Bitmap?,
    cinNumber: String,
    cinDeliveredAt: String,
    onCinFrontClick: () -> Unit,
    onCinBackClick: () -> Unit,
    onCinNumberChange: (String) -> Unit,
    onCinDateChange: (String) -> Unit,
    titleFontSize: TextUnit,
) {
    Text(
        text = stringResource(R.string.driver_personal_title),
        color = OnboardingTitle,
        fontSize = titleFontSize,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.driver_personal_subtitle),
        color = OnboardingSubtitle,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 24.sp
    )
    Spacer(Modifier.height(24.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        UploadPlaceholderCard(
            label = stringResource(R.string.driver_cin_front_label),
            bitmap = cinFrontBmp,
            onClick = onCinFrontClick,
            modifier = Modifier.weight(1f)
        )
        UploadPlaceholderCard(
            label = stringResource(R.string.driver_cin_back_label),
            bitmap = cinBackBmp,
            onClick = onCinBackClick,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(20.dp))

    Text(stringResource(R.string.driver_cin_number_label), color = OnboardingTitle, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = cinNumber,
        onValueChange = { onCinNumberChange(it.filter { ch -> ch.isDigit() }.take(8)) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.driver_cin_number_hint), color = OnboardingLabel.copy(alpha = 0.65f)) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = onboardingFieldColors()
    )
    Text(
        text = stringResource(R.string.driver_cin_digits_hint),
        color = OnboardingSubtitle,
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 6.dp)
    )
    Spacer(Modifier.height(16.dp))

    UnderscoreDateTextField(
        label = stringResource(R.string.driver_cin_date_label),
        value = cinDeliveredAt,
        placeholder = stringResource(R.string.driver_cin_date_hint),
        onValueChange = onCinDateChange
    )
    Spacer(Modifier.height(24.dp))
}
