package tn.turbodrive.presentation.driversetup

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.turbodrive.R
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.theme.LocalAppColors
import tn.turbodrive.domain.usecases.driver.OcrProgress
import java.io.File

/**
 * R-5.2 — File-based camera + gallery capture with inline OCR feedback.
 *
 * Why file-based instead of `TakePicturePreview` (Bitmap thumbnail) :
 *  - Backend OCR needs full-resolution JPEG, not the low-res preview that
 *    the legacy [PhotoSourcePickerDialog] flow produces.
 *  - We still emit a Bitmap to keep the existing submission pipeline
 *    (DriverSetupViewModel.submitDriverSetup) unchanged.
 *
 * Capture flow :
 *  1. User taps the placeholder → camera + gallery row appears
 *  2. Camera path : create temp file under cacheDir/ocr_captures/, share
 *     it through `${applicationId}.fileprovider`, launch TakePicture
 *  3. Gallery path : PickVisualMedia → copy selected URI into cacheDir
 *     so the OCR upload can use a real File handle
 *  4. On success, [onFileCaptured] is invoked with the file ; the caller
 *     is expected to kick off [UploadAndPollOcrUseCase] and surface the
 *     resulting [OcrProgress] back through [ocrProgress]
 */
@Composable
internal fun DocumentOcrCaptureSection(
    label: String,
    bitmap: Bitmap?,
    ocrProgress: OcrProgress?,
    onFileCaptured: (File, Bitmap) -> Unit,
    onRetake: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val file = pendingCameraFile
            pendingCameraFile = null
            if (success && file != null) {
                decodeBitmapFromFile(file)?.let { onFileCaptured(file, it) }
            }
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let { copyToCacheFile(context, it) }?.let { file ->
                decodeBitmapFromFile(file)?.let { onFileCaptured(file, it) }
            }
        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                val (file, uri) = newCameraCaptureTarget(context)
                pendingCameraFile = file
                cameraLauncher.launch(uri)
            }
        }

    fun launchCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            val (file, uri) = newCameraCaptureTarget(context)
            pendingCameraFile = file
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun launchGallery() {
        galleryLauncher.launch(
            androidx.activity.result.PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly,
            ),
        )
    }

    Column(modifier) {
        UploadPlaceholderCard(
            label = label,
            bitmap = bitmap,
            onClick = { if (bitmap == null) launchCamera() else onRetake() },
            modifier = Modifier.fillMaxWidth(),
        )

        if (bitmap == null) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OcrSourcePill(
                    iconRes = AppIcon.camera,
                    label = stringResource(R.string.driver_photo_take_camera),
                    onClick = ::launchCamera,
                    modifier = Modifier.weight(1f),
                )
                OcrSourcePill(
                    iconRes = AppIcon.image,
                    label = stringResource(R.string.driver_photo_take_gallery),
                    onClick = ::launchGallery,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (ocrProgress != null) {
            Spacer(Modifier.height(8.dp))
            OcrProgressBanner(progress = ocrProgress)
        }
    }
}

@Composable
private fun OcrSourcePill(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    Row(
        modifier =
            modifier
                .background(c.surfaceAlt, RoundedCornerShape(12.dp))
                .border(1.dp, c.borderStrong, RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(iconRes), null, tint = c.textPrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(width = 8.dp, height = 0.dp))
        Text(label, color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun OcrProgressBanner(progress: OcrProgress) {
    val c = LocalAppColors.current
    val (bg, fg, message) =
        when (progress) {
            is OcrProgress.Uploading ->
                Triple(c.surfaceAlt, c.textPrimary, stringResource(R.string.driver_ocr_uploading))
            is OcrProgress.Processing ->
                Triple(c.surfaceAlt, c.textPrimary, stringResource(R.string.driver_ocr_processing))
            is OcrProgress.Ready ->
                Triple(c.accentSoft, c.accentInk, stringResource(R.string.driver_ocr_ready))
            is OcrProgress.UploadFailed ->
                Triple(c.errorSoft, c.error, stringResource(R.string.driver_ocr_upload_failed))
            is OcrProgress.PollFailed ->
                Triple(c.errorSoft, c.error, stringResource(R.string.driver_ocr_poll_failed))
            is OcrProgress.OcrFailed ->
                Triple(c.errorSoft, c.error, stringResource(R.string.driver_ocr_failed))
            OcrProgress.Timeout ->
                Triple(c.errorSoft, c.error, stringResource(R.string.driver_ocr_timeout))
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(bg, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (progress) {
            OcrProgress.Uploading, is OcrProgress.Processing ->
                CircularProgressIndicator(
                    color = fg,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp),
                )
            is OcrProgress.Ready ->
                Icon(painterResource(AppIcon.check), null, tint = fg, modifier = Modifier.size(18.dp))
            else ->
                Icon(painterResource(AppIcon.close), null, tint = fg, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.size(width = 10.dp, height = 0.dp))
        Text(message, color = fg, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

private data class CameraTarget(val file: File, val uri: Uri)

private operator fun CameraTarget.component1(): File = file

private operator fun CameraTarget.component2(): Uri = uri

private fun newCameraCaptureTarget(context: Context): CameraTarget {
    val dir = File(context.cacheDir, "ocr_captures").apply { mkdirs() }
    val file = File(dir, "ocr_${System.currentTimeMillis()}.jpg")
    val authority = "${context.packageName}.fileprovider"
    val uri = FileProvider.getUriForFile(context, authority, file)
    return CameraTarget(file = file, uri = uri)
}

private fun copyToCacheFile(
    context: Context,
    uri: Uri,
): File? =
    runCatching {
        val dir = File(context.cacheDir, "ocr_captures").apply { mkdirs() }
        val dest = File(dir, "ocr_gallery_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        dest.takeIf { it.length() > 0 }
    }.getOrNull()

private fun decodeBitmapFromFile(file: File): Bitmap? =
    runCatching {
        BitmapFactory.decodeFile(file.absolutePath)
    }.getOrNull()

/**
 * Side-effect : if the upstream OCR result eventually arrives in a non-active
 * step, the progress banner stays mounted because we keep the source of truth
 * in the parent VM. This LaunchedEffect lets the parent observe banner
 * dismissals from the captured side ; no-op when [ocrProgress] is null.
 */
@Suppress("unused")
@Composable
internal fun rememberOcrProgressDismiss(
    progress: OcrProgress?,
    onDismiss: () -> Unit,
) {
    LaunchedEffect(progress) {
        if (progress is OcrProgress.Ready) {
            kotlinx.coroutines.delay(3_000)
            onDismiss()
        }
    }
}
