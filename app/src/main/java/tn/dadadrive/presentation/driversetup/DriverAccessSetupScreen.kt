package tn.dadadrive.presentation.driversetup

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.dadadrive.R
import tn.dadadrive.core.theme.LocalAppColors

@Composable
fun DriverAccessSetupScreen(
    onClose: () -> Unit,
    onGoToSettingsFlow: () -> Unit,
    onOpenTaxiLicenseUpload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = stringResource(R.string.driver_permission_close),
                fontSize = 18.sp,
                modifier = Modifier.clickable(onClick = onClose)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(onClick = onClose),
                tint = Color(0xFF111111)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.driver_permission_intro_title),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 30.sp
        )
        Spacer(Modifier.height(18.dp))

        AccessStep(
            iconBg = Color(0xFF0AAE3A),
            iconTint = Color.White,
            icon = { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(17.dp)) },
            title = stringResource(R.string.driver_permission_step_done_title),
            description = stringResource(R.string.driver_permission_step_done_desc),
            showTail = true
        )
        AccessStep(
            iconBg = Color(0xFF222222),
            iconTint = Color.White,
            icon = { Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(17.dp)) },
            title = stringResource(R.string.driver_permission_step_access_title),
            description = stringResource(R.string.driver_permission_step_access_desc),
            showTail = true
        )
        AccessStep(
            iconBg = Color(0xFFE9E9E9),
            iconTint = Color(0xFF2A2A2A),
            icon = { Icon(Icons.Outlined.DirectionsCar, contentDescription = null, modifier = Modifier.size(17.dp)) },
            title = stringResource(R.string.driver_permission_step_wait_title),
            description = stringResource(R.string.driver_permission_step_wait_desc),
            showTail = false
        )

        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.driver_permission_optional_docs_title),
            fontWeight = FontWeight.SemiBold,
            fontSize = 19.sp,
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenTaxiLicenseUpload() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                tint = Color(0xFF333333),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.size(12.dp))
            Text(
                text = stringResource(R.string.driver_permission_optional_taxi_license),
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF333333)
            )
        }

        Spacer(Modifier.weight(1f))
        Button(
            onClick = onGoToSettingsFlow,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Text(
                stringResource(R.string.driver_permission_go_settings),
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp
            )
        }
    }
}

@Composable
fun DriverTaxiLicenseUploadScreen(
    onClose: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var taxiFrontBmp by remember { mutableStateOf<Bitmap?>(null) }
    var taxiBackBmp by remember { mutableStateOf<Bitmap?>(null) }
    var activeSlot by remember { mutableStateOf<PhotoSlot?>(null) }
    var showSourcePicker by remember { mutableStateOf(false) }

    fun applyBitmap(bmp: Bitmap) {
        when (activeSlot) {
            PhotoSlot.LicenseFront -> taxiFrontBmp = bmp
            PhotoSlot.LicenseBack -> taxiBackBmp = bmp
            else -> {}
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { context.uriToBitmap(it)?.let(::applyBitmap) }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        bmp?.let(::applyBitmap)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraLauncher.launch(null)
    }

    if (showSourcePicker) {
        PhotoSourcePickerDialog(
            onCamera = {
                showSourcePicker = false
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraLauncher.launch(null)
                } else {
                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            },
            onGallery = {
                showSourcePicker = false
                galleryLauncher.launch("image/*")
            },
            onDismiss = { showSourcePicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(text = stringResource(R.string.driver_permission_close), fontSize = 18.sp, modifier = Modifier.clickable(onClick = onClose))
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(22.dp).clickable(onClick = onBack),
                tint = Color(0xFF111111)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.driver_taxi_license_upload_title),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 30.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.driver_taxi_license_upload_subtitle),
            color = Color(0xFF666666),
            fontSize = 15.sp
        )
        Spacer(Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            UploadPlaceholderCard(
                label = stringResource(R.string.driver_taxi_license_front),
                bitmap = taxiFrontBmp,
                onClick = { activeSlot = PhotoSlot.LicenseFront; showSourcePicker = true },
                modifier = Modifier.weight(1f)
            )
            UploadPlaceholderCard(
                label = stringResource(R.string.driver_taxi_license_back),
                bitmap = taxiBackBmp,
                onClick = { activeSlot = PhotoSlot.LicenseBack; showSourcePicker = true },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AccessStep(
    iconBg: Color,
    iconTint: Color,
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    showTail: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconBg, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides iconTint
                ) { icon() }
            }
            if (showTail) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .size(width = 2.dp, height = 44.dp)
                        .background(Color(0xFFBDBDBD))
                )
            }
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.padding(top = 2.dp)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp)
            Text(
                text = description,
                color = Color(0xFF6F6F6F),
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun DriverPhoneSettingsScreen(
    onClose: () -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current

    fun openIntent(intent: Intent) {
        runCatching { context.startActivity(intent) }
    }

    val packageUri = Uri.parse("package:${context.packageName}")

    val openOverlay = {
        openIntent(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, packageUri))
    }
    val openLocation = {
        openIntent(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri))
    }
    val openNotifications = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            openIntent(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
            )
        } else {
            openIntent(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri))
        }
    }
    val openBattery = {
        openIntent(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
    }
    val openAutostart = {
        openIntent(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                text = stringResource(R.string.driver_permission_close),
                fontSize = 20.sp,
                modifier = Modifier.clickable(onClick = onClose)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.driver_permission_settings_title),
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            lineHeight = 38.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.driver_permission_settings_subtitle),
            color = Color(0xFF444444),
            fontSize = 17.sp
        )
        Spacer(Modifier.height(20.dp))

        PermissionRow(title = stringResource(R.string.driver_permission_overlay), onClick = openOverlay)
        PermissionRow(title = stringResource(R.string.driver_permission_location), onClick = openLocation)
        PermissionRow(title = stringResource(R.string.driver_permission_notifications), onClick = openNotifications)
        PermissionRow(title = stringResource(R.string.driver_permission_battery), onClick = openBattery)
        PermissionRow(title = stringResource(R.string.driver_permission_autostart), onClick = openAutostart)

        Spacer(Modifier.weight(1f))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Text(
                stringResource(R.string.driver_permission_done),
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
            )
        }
    }
}

@Composable
fun DriverVerificationPendingScreen(
    onClose: () -> Unit,
    onContinueAsPassenger: () -> Unit,
    onBackToHome: () -> Unit,
) {
    val pageBg = Color(0xFFF7F6F2)
    val successGreen = Color(0xFF16A34A)
    val successBg = Color(0xFFE8F5E9)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
                shadowElevation = 2.dp,
                onClick = onClose
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.driver_close),
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(successBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = successGreen,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.driver_verification_documents_sent),
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                lineHeight = 32.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.driver_verification_documents_sent_desc),
                color = Color(0xFF666666),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF3F3F3)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = null,
                                tint = Color(0xFF888888),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.driver_verification_notification_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.driver_verification_notification_desc),
                            fontSize = 14.sp,
                            color = Color(0xFF777777),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onContinueAsPassenger,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(
                    stringResource(R.string.driver_continue_as_passenger),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            TextButton(
                onClick = onBackToHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.driver_back_to_home),
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .background(Color(0xFFF1F1F1), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.driver_permission_open_action),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}
