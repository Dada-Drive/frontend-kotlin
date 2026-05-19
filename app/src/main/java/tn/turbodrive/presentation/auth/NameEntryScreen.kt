package tn.turbodrive.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.turbodrive.R
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.theme.LocalAppColors
import tn.turbodrive.presentation.common.ScreenState

private val ScreenBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.background

private val Muted: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textSecondary

@Composable
fun NameEntryScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit = {},
    viewModel: NameEntryViewModel = hiltViewModel(),
) {
    val c = LocalAppColors.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()
    val loading: Boolean =
        when (state) {
            ScreenState.Loading -> true
            ScreenState.Idle, is ScreenState.Loaded, is ScreenState.Error -> false
        }
    val error: String? =
        when (val s = state) {
            is ScreenState.Error -> s.error.message
            ScreenState.Idle, ScreenState.Loading, is ScreenState.Loaded -> null
        }

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
                        .background(c.surface)
                        .border(1.dp, c.border, CircleShape)
                        .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(AppIcon.arrowLeft),
                    contentDescription = null,
                    tint = c.textPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = stringResource(R.string.auth_registration_step, 3, 4),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = c.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.width(40.dp))
        }
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.name_entry_title),
                color = c.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.name_entry_subtitle),
                color = Muted,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
            Spacer(Modifier.height(28.dp))
            Text(
                stringResource(R.string.auth_full_name),
                color = c.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(painterResource(AppIcon.user), null, tint = Muted)
                },
                shape = RoundedCornerShape(12.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = c.inkSoft,
                        unfocusedBorderColor = c.border,
                        focusedTextColor = c.textPrimary,
                        unfocusedTextColor = c.textPrimary,
                        focusedContainerColor = c.surface,
                        unfocusedContainerColor = c.surface,
                        cursorColor = c.textPrimary,
                    ),
            )
            Spacer(Modifier.height(20.dp))
            Text(
                stringResource(R.string.name_email_optional_label),
                color = c.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.name_email_placeholder), color = c.borderStrong) },
                leadingIcon = {
                    Icon(painterResource(AppIcon.mail), null, tint = Muted)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = c.border,
                        unfocusedBorderColor = c.border,
                        focusedTextColor = c.textPrimary,
                        unfocusedTextColor = c.textPrimary,
                        focusedContainerColor = c.surface,
                        unfocusedContainerColor = c.surface,
                        cursorColor = c.textPrimary,
                    ),
            )
            Text(
                stringResource(R.string.name_email_helper),
                color = Muted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
            error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = c.error, fontSize = 13.sp)
            }
            Spacer(Modifier.height(32.dp))
        }
        Button(
            onClick = { viewModel.submitFullName(name, email, onContinue) },
            enabled = !loading && name.isNotBlank(),
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
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    stringResource(R.string.auth_continue),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}
