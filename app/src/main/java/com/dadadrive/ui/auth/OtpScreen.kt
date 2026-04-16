package com.dadadrive.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.dadadrive.R
import com.dadadrive.core.constants.Constants
import com.dadadrive.ui.theme.LocalAppColors
import kotlinx.coroutines.delay

private const val RESEND_DELAY_SECONDS = 120

@Composable
fun OtpScreen(
    phone: String,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val appColors = LocalAppColors.current
    val authState by authViewModel.authState.collectAsState()
    val isDarkUi = appColors.background.luminance() < 0.5f
    val bg = MaterialTheme.colorScheme.background
    val fg = MaterialTheme.colorScheme.onBackground

    var code by remember { mutableStateOf("") }
    var secondsLeft by remember { mutableIntStateOf(RESEND_DELAY_SECONDS) }
    val focusRequester = remember { FocusRequester() }

    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message
    val canSubmit = code.length == Constants.PHONE_CODE_LENGTH && !isLoading

    // Ouvre le clavier automatiquement à l'arrivée sur l'écran
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    // Countdown
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        }
    }

    // Navigation au succès
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onSuccess()
            authViewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Bouton retour
            IconButton(onClick = {
                authViewModel.resetState()
                onBack()
            }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = fg
                )
            }

            Spacer(Modifier.height(32.dp))

            // Titre
            Text(
                text = "Saisissez le code",
                color = fg,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp
            )

            Spacer(Modifier.height(12.dp))

            // Sous-titre
            Text(
                text = "Nous vous avons envoyé un code par SMS au",
                color = fg.copy(alpha = 0.6f),
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
            Text(
                text = phone,
                color = appColors.primary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(48.dp))

            // ── Zone de saisie OTP ──────────────────────────
            //
            // Architecture :
            //   Box cliquable (hauteur 68dp)
            //     ├── BasicTextField invisible (alpha=0, couvre toute la zone)
            //     │     → capture le clavier physiquement
            //     └── Row de 6 cases visuelles (affichage uniquement)
            //
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .clickable { focusRequester.requestFocus() }
            ) {
                // Champ caché — reçoit vraiment la saisie
                BasicTextField(
                    value = code,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= Constants.PHONE_CODE_LENGTH) {
                            code = digits
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    cursorBrush = SolidColor(Color.Transparent),
                    textStyle = TextStyle(color = Color.Transparent, fontSize = 1.sp),
                    singleLine = true,
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0f)
                        .focusRequester(focusRequester)
                )

                // Cases visuelles — affichage seulement
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    repeat(Constants.PHONE_CODE_LENGTH) { index ->
                        val digit = code.getOrNull(index)?.toString() ?: ""
                        val isFocused = index == code.length
                        val isFilled = digit.isNotEmpty()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isFilled -> appColors.primary.copy(
                                            alpha = if (isDarkUi) 0.15f else 0.08f
                                        )
                                        isDarkUi -> Color.White.copy(alpha = 0.05f)
                                        else -> Color.Black.copy(alpha = 0.03f)
                                    }
                                )
                                .border(
                                    width = if (isFocused || isFilled) 2.dp else 1.dp,
                                    color = when {
                                        isFilled -> appColors.primary
                                        isFocused -> appColors.primary.copy(alpha = 0.6f)
                                        else -> fg.copy(alpha = 0.15f)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isFilled) {
                                Text(
                                    text = digit,
                                    color = appColors.primary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            } else if (isFocused) {
                                // Curseur clignotant simulé
                                Text(
                                    text = "|",
                                    color = appColors.primary.copy(alpha = 0.8f),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Light,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Message d'erreur
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                errorMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LocalAppColors.current.errorContainer, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = msg,
                            color = LocalAppColors.current.onErrorContainer,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // ── Section bas de page ──────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Renvoi du code
            if (secondsLeft > 0) {
                val minutes = secondsLeft / 60
                val seconds = secondsLeft % 60
                Text(
                    text = stringResource(R.string.otp_resend_timer, minutes, seconds),
                    color = fg.copy(alpha = 0.4f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                TextButton(onClick = {
                    code = ""
                    secondsLeft = RESEND_DELAY_SECONDS
                    authViewModel.resetState()
                    authViewModel.sendOtp(phone)
                }) {
                    Text(
                        text = stringResource(R.string.otp_resend),
                        color = appColors.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Bouton Continuer
            Button(
                onClick = { if (canSubmit) authViewModel.verifyOtp(phone, code) },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors.buttonBackground,
                    contentColor = appColors.buttonText,
                    disabledContainerColor = appColors.buttonDisabledBackground,
                    disabledContentColor = appColors.buttonDisabledText
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = appColors.buttonText,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.auth_verifying), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                } else {
                    Text(stringResource(R.string.auth_continue), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.auth_flow_step, 2, 6),
                color = fg.copy(alpha = 0.3f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
