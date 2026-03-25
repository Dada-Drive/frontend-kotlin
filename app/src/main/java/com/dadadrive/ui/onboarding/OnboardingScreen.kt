package com.dadadrive.ui.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadrive.R
import com.dadadrive.ui.theme.DadaDriveGreen

// ─────────────────────────────────────────────────────────
// ENTRY POINT
// ─────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val goNext: () -> Unit = { if (currentPage < 3) currentPage++ else onFinished() }

    when (currentPage) {
        0 -> PageRapide(page = 0, onNext = goNext, onSkip = onFinished)
        1 -> PageSecurite(page = 1, onNext = goNext, onSkip = onFinished)
        2 -> PagePrix(page = 2, onNext = goNext, onSkip = onFinished)
        3 -> PageLocalisation(onActivate = onFinished, onSkip = onFinished)
    }
}

// ─────────────────────────────────────────────────────────
// PAGE 1 — RAPIDE. SIMPLE. SANS TRACAS.
// ─────────────────────────────────────────────────────────

@Composable
private fun PageRapide(page: Int, onNext: () -> Unit, onSkip: () -> Unit) {
    val bg = MaterialTheme.colorScheme.background
    val fg = MaterialTheme.colorScheme.onBackground

    Box(modifier = Modifier.fillMaxSize().background(bg)) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 148.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(52.dp))
            OnboardingHeader(page = page, onSkip = onSkip)
            Spacer(Modifier.height(20.dp))

            Image(
                painter = painterResource(id = R.drawable.rapiditer),
                contentDescription = "Rapidité",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(24.dp))
            )

            Spacer(Modifier.height(32.dp))
            Text(
                text = "RAPIDE.\nSIMPLE.\nSANS TRACAS.",
                color = fg,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 48.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Dada Drive vous emmène là où vous devez être, en un clin d'œil.",
                color = fg.copy(alpha = 0.6f),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
        }

        BottomButtonSection(bg = bg, modifier = Modifier.align(Alignment.BottomCenter)) {
            OnboardingButton(label = "SUIVANT  →", onClick = onNext)
            Spacer(Modifier.height(12.dp))
            SkipLink(label = "PASSER L'INTRODUCTION", onClick = onSkip)
        }
    }
}

// ─────────────────────────────────────────────────────────
// PAGE 2 — VOTRE SÉCURITÉ, NOTRE PRIORITÉ.
// ─────────────────────────────────────────────────────────

@Composable
private fun PageSecurite(page: Int, onNext: () -> Unit, onSkip: () -> Unit) {
    val bg = MaterialTheme.colorScheme.background
    val fg = MaterialTheme.colorScheme.onBackground

    Box(modifier = Modifier.fillMaxSize().background(bg)) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 148.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(52.dp))

            Image(
                painter = painterResource(id = R.drawable.confiance),
                contentDescription = "La confiance",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(24.dp))
            )

            Spacer(Modifier.height(28.dp))
            Text(
                text = "Votre sécurité,\nnotre priorité.",
                color = fg,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 40.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))

            FeatureItem(Icons.Default.Shield, "Chauffeurs certifiés", "Vérification rigoureuse et continue.")
            Spacer(Modifier.height(16.dp))
            FeatureItem(Icons.Default.LocationOn, "Suivi en temps réel", "Partagez votre trajet avec vos proches.")
            Spacer(Modifier.height(16.dp))
            FeatureItem(Icons.Default.Phone, "Assistance 24/7", "Une équipe dédiée à votre écoute.")
            Spacer(Modifier.height(24.dp))

            DotsIndicator(totalDots = 4, selectedIndex = page)
            Spacer(Modifier.height(24.dp))
        }

        BottomButtonSection(bg = bg, modifier = Modifier.align(Alignment.BottomCenter)) {
            OnboardingButton(label = "CONTINUER", onClick = onNext)
            Spacer(Modifier.height(12.dp))
            SkipLink(label = "Ignorer", onClick = onSkip)
        }
    }
}

// ─────────────────────────────────────────────────────────
// PAGE 3 — PAS DE MAUVAISES SURPRISES.
// ─────────────────────────────────────────────────────────

@Composable
private fun PagePrix(page: Int, onNext: () -> Unit, onSkip: () -> Unit) {
    val bg = MaterialTheme.colorScheme.background
    val fg = MaterialTheme.colorScheme.onBackground

    Box(modifier = Modifier.fillMaxSize().background(bg)) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 148.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(52.dp))
            ReceiptCard()
            Spacer(Modifier.height(32.dp))
            Text(
                text = "Pas de mauvaises surprises.",
                color = fg,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 42.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Le prix est fixé avant même de commander. Vous savez exactement ce que vous payez, peu importe le trafic.",
                color = fg.copy(alpha = 0.6f),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
            DotsIndicator(totalDots = 4, selectedIndex = page)
            Spacer(Modifier.height(24.dp))
        }

        BottomButtonSection(bg = bg, modifier = Modifier.align(Alignment.BottomCenter)) {
            OnboardingButton(label = "COMPRIS  →", onClick = onNext)
            Spacer(Modifier.height(12.dp))
            SkipLink(label = "Ignorer", onClick = onSkip)
        }
    }
}

// ─────────────────────────────────────────────────────────
// PAGE 4 — ON Y VA ?
// ─────────────────────────────────────────────────────────

@Composable
private fun PageLocalisation(onActivate: () -> Unit, onSkip: () -> Unit) {
    val bg = MaterialTheme.colorScheme.background
    val fg = MaterialTheme.colorScheme.onBackground

    Box(modifier = Modifier.fillMaxSize().background(bg)) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 160.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onSkip) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = fg
                    )
                }
                Text(
                    text = "ÉTAPE 04/04",
                    color = fg.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.height(32.dp))
            RadarAnimation(modifier = Modifier.size(220.dp))
            Spacer(Modifier.height(40.dp))
            Text(
                text = "ON Y VA ?",
                color = fg,
                fontSize = 52.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Autorisez l'accès à votre position pour que nous puissions vous trouver rapidement.",
                color = fg.copy(alpha = 0.6f),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "SÉCURITÉ  ·  PROXIMITÉ  ·  DADA DRIVE",
                color = fg.copy(alpha = 0.3f),
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
        }

        BottomButtonSection(bg = bg, modifier = Modifier.align(Alignment.BottomCenter)) {
            OnboardingButton(label = "ACTIVER LA LOCALISATION  ▶", onClick = onActivate)
            Spacer(Modifier.height(12.dp))
            SkipLink(label = "PLUS TARD", onClick = onSkip)
        }
    }
}

// ─────────────────────────────────────────────────────────
// SHARED COMPONENTS
// ─────────────────────────────────────────────────────────

/** Fixed bottom area — gradient fade + button content */
@Composable
private fun BottomButtonSection(
    bg: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(bg.copy(alpha = 0f), bg, bg),
                    startY = 0f,
                    endY = 120f
                )
            )
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp, top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
private fun OnboardingHeader(page: Int, onSkip: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_dadadrive_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(7.dp))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Dada Drive",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
        DotsIndicator(totalDots = 4, selectedIndex = page)
    }
}

@Composable
private fun DotsIndicator(totalDots: Int, selectedIndex: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalDots) { index ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(if (selected) 22.dp else 6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (selected) DadaDriveGreen
                        else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )
            )
        }
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, title: String, subtitle: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(DadaDriveGreen.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = DadaDriveGreen, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(text = title, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f), fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun ReceiptCard() {
    val fg = MaterialTheme.colorScheme.onBackground
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text("DADA", color = fg, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                    Text("le n° 8929-K.", color = fg.copy(alpha = 0.4f), fontSize = 12.sp, fontStyle = FontStyle.Italic)
                }
                Box(
                    modifier = Modifier.background(DadaDriveGreen, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("FIXED", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("0€", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Text("SURPRISE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = fg.copy(alpha = 0.1f))
            Spacer(Modifier.height(12.dp))
            ReceiptRow("Trajet Standard", "24.50€", fg)
            Spacer(Modifier.height(8.dp))
            ReceiptRow("Frais d'accès", "0.00€", fg)
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = fg.copy(alpha = 0.1f))
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("TOTAL FIXÉ", color = fg, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                Text("24.50€", color = fg, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.background(DadaDriveGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Verified, null, tint = DadaDriveGreen, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("PRIX GARANTI", color = DadaDriveGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, amount: String, fg: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = fg.copy(alpha = 0.6f), fontSize = 14.sp)
        Text(text = amount, color = fg, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun OnboardingButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DadaDriveGreen, contentColor = Color.White)
    ) {
        Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
private fun SkipLink(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.clickable { onClick() }
    )
}

// ─────────────────────────────────────────────────────────
// CANVAS — RADAR ANIMATION
// ─────────────────────────────────────────────────────────

@Composable
private fun RadarAnimation(modifier: Modifier = Modifier) {
    val tr = rememberInfiniteTransition(label = "radar")
    val s1 by tr.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart), label = "s1")
    val s2 by tr.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, delayMillis = 800, easing = LinearEasing), RepeatMode.Restart), label = "s2")
    val s3 by tr.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, delayMillis = 1600, easing = LinearEasing), RepeatMode.Restart), label = "s3")

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f
        listOf(s1, s2, s3).forEach { s ->
            drawCircle(color = DadaDriveGreen.copy(alpha = (1f - s) * 0.5f), radius = maxR * s, center = center, style = Stroke(width = 2.dp.toPx()))
        }
        drawCircle(color = DadaDriveGreen.copy(alpha = 0.15f), radius = maxR * 0.25f, center = center)
        drawCircle(color = DadaDriveGreen, radius = 10.dp.toPx(), center = center)
        drawLine(color = DadaDriveGreen, start = center, end = Offset(center.x, center.y + 18.dp.toPx()), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
    }
}
