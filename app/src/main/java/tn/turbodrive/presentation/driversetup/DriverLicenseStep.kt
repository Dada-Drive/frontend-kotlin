package tn.turbodrive.presentation.driversetup

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbodrive.R
import tn.turbodrive.core.theme.LocalAppColors
import tn.turbodrive.domain.usecases.driver.OcrProgress
import java.io.File

/**
 * R-5.2 — Driver setup step 2, "Permis de conduire".
 *
 * The license front slot uses [DocumentOcrCaptureSection] to capture the
 * front side at full resolution and run OCR (numero, date_expiration,
 * categories). The back slot stays on the legacy Bitmap-only path because
 * the OCR endpoint only reads the front.
 *
 * Visual spec : `turbodrive_redesign/screens-setup.jsx:87-122` (Step2License).
 */
@Composable
internal fun DriverLicenseStep(
    licenseFrontBmp: Bitmap?,
    licenseBackBmp: Bitmap?,
    licenseSuffix: String,
    licenseIssueInput: String,
    licenseExpiryInput: String,
    licenseCategories: Set<Char>,
    permisOcrProgress: OcrProgress?,
    onLicenseFrontFileCaptured: (File, Bitmap) -> Unit,
    onLicenseFrontRetake: () -> Unit,
    onLicenseBackClick: () -> Unit,
    onSuffixChange: (String) -> Unit,
    onIssueChange: (String) -> Unit,
    onExpiryChange: (String) -> Unit,
    onLicenseCategoryToggle: (Char) -> Unit,
    titleFontSize: TextUnit,
) {
    Text(
        text = stringResource(R.string.driver_license_title),
        color = OnboardingTitle,
        fontSize = titleFontSize,
        fontWeight = FontWeight.Bold,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.driver_license_subtitle),
        color = OnboardingSubtitle,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    )
    Spacer(Modifier.height(24.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            DocumentOcrCaptureSection(
                label = stringResource(R.string.driver_license_front_label),
                bitmap = licenseFrontBmp,
                ocrProgress = permisOcrProgress,
                onFileCaptured = onLicenseFrontFileCaptured,
                onRetake = onLicenseFrontRetake,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            UploadPlaceholderCard(
                label = stringResource(R.string.driver_license_back_label),
                bitmap = licenseBackBmp,
                onClick = onLicenseBackClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    Spacer(Modifier.height(18.dp))

    Text(stringResource(R.string.driver_license_number_label), color = OnboardingTitle, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = licenseSuffix,
        onValueChange = onSuffixChange,
        modifier = Modifier.fillMaxWidth(),
        prefix = { Text("TN-", color = OnboardingTitle, fontWeight = FontWeight.Medium) },
        placeholder = { Text(stringResource(R.string.driver_license_suffix_hint), color = OnboardingLabel.copy(alpha = 0.65f)) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = onboardingFieldColors(),
    )
    Spacer(Modifier.height(20.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            UnderscoreDateTextField(
                label = stringResource(R.string.driver_license_issue_label),
                value = licenseIssueInput,
                placeholder = stringResource(R.string.driver_license_date_hint),
                onValueChange = onIssueChange,
                fieldModifier = Modifier.fillMaxWidth(),
            )
        }
        Column(Modifier.weight(1f)) {
            UnderscoreDateTextField(
                label = stringResource(R.string.driver_license_expiry_label),
                value = licenseExpiryInput,
                placeholder = stringResource(R.string.driver_license_date_hint),
                onValueChange = onExpiryChange,
                fieldModifier = Modifier.fillMaxWidth(),
            )
        }
    }
    Spacer(Modifier.height(20.dp))

    Text(
        stringResource(R.string.driver_license_category_label),
        color = OnboardingTitle,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(Modifier.height(10.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        listOf('A', 'B', 'C', 'D').forEach { cat ->
            val selected = cat in licenseCategories
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selected) MonoPrimary else Color.White)
                        .border(
                            width = 1.dp,
                            color = if (selected) MonoPrimary else LocalAppColors.current.border,
                            shape = RoundedCornerShape(14.dp),
                        )
                        .clickable { onLicenseCategoryToggle(cat) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = cat.toString(),
                    color = if (selected) MonoOnPrimary else OnboardingTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
    Spacer(Modifier.height(24.dp))
}
