package tn.turbodrive.presentation.role

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.turbodrive.R
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.theme.LocalAppColors

private val ScreenBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.background

private val AccentGreen: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.accent

private val InfoGray: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textSubtle

@Composable
fun RoleSelectionScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit = {},
    viewModel: RoleViewModel = hiltViewModel(),
) {
    var selectedRole by remember { mutableStateOf<String?>("rider") }
    val state by viewModel.state.collectAsState()
    var hasNavigatedOnSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is RoleViewModel.RoleState.Success && !hasNavigatedOnSuccess) {
            hasNavigatedOnSuccess = true
            onSuccess()
        }
    }

    val c = LocalAppColors.current
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(ScreenBg)
                .statusBarsPadding()
                .navigationBarsPadding(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, c.border, CircleShape)
                        .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(AppIcon.arrowLeft),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = stringResource(R.string.auth_registration_step, 4, 4),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.width(40.dp))
        }
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.role_selection_headline),
                color = Color.Black,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.role_selection_subtitle),
                color = c.textSecondary,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
            Spacer(Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TurboRoleCard(
                    title = stringResource(R.string.role_passenger_title),
                    subtitle = stringResource(R.string.role_passenger_subtitle),
                    icon = AppIcon.user,
                    selected = selectedRole == "rider",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedRole = "rider" },
                )
                TurboRoleCard(
                    title = stringResource(R.string.role_driver_title),
                    subtitle = stringResource(R.string.role_driver_subtitle),
                    icon = AppIcon.car,
                    selected = selectedRole == "driver",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedRole = "driver" },
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(c.surfaceAlt)
                        .padding(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    painter = painterResource(AppIcon.info),
                    contentDescription = null,
                    tint = InfoGray,
                    modifier = Modifier.size(18.dp).padding(top = 2.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.role_driver_info_note),
                    color = InfoGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }
        }
        Spacer(Modifier.weight(1f))
        if (state is RoleViewModel.RoleState.Error) {
            Text(
                text = (state as RoleViewModel.RoleState.Error).message,
                color = c.error,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }
        val isLoading = state is RoleViewModel.RoleState.Loading
        val cta =
            when (selectedRole) {
                "driver" -> stringResource(R.string.role_continue_driver)
                else -> stringResource(R.string.role_continue_passenger)
            }
        Button(
            onClick = { selectedRole?.let { viewModel.selectRole(it) } },
            enabled = selectedRole != null && !isLoading,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(54.dp),
            shape = RoundedCornerShape(999.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    disabledContainerColor = c.border,
                    disabledContentColor = c.textSubtle,
                ),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(cta, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun TurboRoleCard(
    title: String,
    subtitle: String,
    @DrawableRes icon: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) Color.Black else Color.White
    val titleColor = if (selected) Color.White else Color.Black
    val c = LocalAppColors.current
    val subColor = if (selected) Color.White.copy(alpha = 0.85f) else c.textSecondary
    val iconBg = if (selected) c.textPrimary else c.surfaceAlt
    val iconTint = if (selected) Color.White else Color.Black
    Column(
        modifier =
            modifier
                .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x22000000), spotColor = Color(0x22000000))
                .clip(RoundedCornerShape(18.dp))
                .background(bg)
                .border(1.dp, if (selected) Color.Black else Color(0x11000000), RoundedCornerShape(18.dp))
                .clickable(onClick = onClick)
                .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(icon), contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            if (selected) {
                Box(
                    modifier =
                        Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(AccentGreen),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(AppIcon.check),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(title, color = titleColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = subColor, fontSize = 13.sp, lineHeight = 18.sp)
    }
}
