package com.dadadrive.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadrive.R
import com.dadadrive.ui.theme.LocalAppColors

@Composable
fun WelcomeScreen(
    onPhoneClick: () -> Unit,
    onGoogleClick: () -> Unit,
    authState: AuthState = AuthState.Idle
) {
    val appColors = LocalAppColors.current
    val bg = MaterialTheme.colorScheme.background
    val fg = MaterialTheme.colorScheme.onBackground
    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    Box(modifier = Modifier.fillMaxSize().background(bg)) {

        // ── Scrollable content (leaves space for fixed bottom) ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 196.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Hero image with gradient fade ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.welcomepage),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.45f to Color.Transparent,
                            1f to bg
                        )
                    )
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_dadadrive_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(30.dp).clip(RoundedCornerShape(7.dp))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "DADA DRIVE",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }

            // ── Headline + subtitle + dots ──
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = buildAnnotatedString {
                        append("Votre application pour des ")
                        withStyle(
                            SpanStyle(
                                color = appColors.primary,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Bold
                            )
                        ) { append("courses équitables") }
                    },
                    color = fg,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 36.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Choisissez les courses qui vous conviennent.",
                    color = fg.copy(alpha = 0.6f),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(22.dp))
                Row {
                    repeat(3) { i ->
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (i == 0) 22.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (i == 0) appColors.primary else fg.copy(alpha = 0.2f)
                                )
                        )
                        if (i < 2) Spacer(Modifier.width(5.dp))
                    }
                }
            }
        }

        // ── Fixed bottom: boutons + message d'erreur + texte légal ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(bg.copy(alpha = 0f), bg, bg),
                        startY = 0f,
                        endY = 80f
                    )
                )
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Message d'erreur visible si Google auth échoue
            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = appColors.errorContainer,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = appColors.onErrorContainer,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // Continuer par téléphone
            Button(
                onClick = onPhoneClick,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors.buttonBackground,
                    contentColor = appColors.buttonText,
                    disabledContainerColor = appColors.buttonDisabledBackground,
                    disabledContentColor = appColors.buttonDisabledText
                )
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Continuer par téléphone", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(Modifier.height(12.dp))

            // Continuer avec Google
            OutlinedButton(
                onClick = onGoogleClick,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, fg.copy(alpha = if (isLoading) 0.1f else 0.25f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = fg)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = appColors.googleRed,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Connexion en cours...", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                } else {
                    Text("G", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = appColors.googleRed)
                    Spacer(Modifier.width(10.dp))
                    Text("Continuer avec Google", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "EN CONTINUANT, VOUS ACCEPTEZ NOS CONDITIONS D'UTILISATION ET NOTRE POLITIQUE DE CONFIDENTIALITÉ",
                color = fg.copy(alpha = 0.3f),
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                letterSpacing = 0.3.sp,
                lineHeight = 13.sp
            )
        }
    }
}
