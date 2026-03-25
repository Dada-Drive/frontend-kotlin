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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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

    val goNext: () -> Unit = {
        if (currentPage < 3) currentPage++ else onFinished()
    }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(52.dp))

        // Header
        OnboardingHeader(page = page, onSkip = onSkip)

        Spacer(Modifier.height(20.dp))

        // Image area with badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF0A0E27), Color(0xFF1B2A4A), Color(0xFF0D1F3C))
                    )
                )
        ) {
            // Speed lines (decorative)
            SpeedLinesCanvas(modifier = Modifier.fillMaxSize())

            // Badge top-right
            GreenBadge(
                icon = Icons.Default.ElectricBolt,
                label = "VITESSE\nRAPIDE",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        // Impact text
        Text(
            text = "RAPIDE.\nSIMPLE.\nSANS TRACAS.",
            color = fg,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 48.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Dada Drive vous emmène là où vous devez être, en un clin d'œil.",
            color = fg.copy(alpha = 0.6f),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(36.dp))

        OnboardingButton(label = "SUIVANT  →", onClick = onNext)

        Spacer(Modifier.height(16.dp))

        SkipLink(label = "PASSER L'INTRODUCTION", onClick = onSkip)

        Spacer(Modifier.height(32.dp))
    }
}

// ─────────────────────────────────────────────────────────
// PAGE 2 — VOTRE SÉCURITÉ, NOTRE PRIORITÉ.
// ─────────────────────────────────────────────────────────

@Composable
private fun PageSecurite(page: Int, onNext: () -> Unit, onSkip: () -> Unit) {
    val bg = MaterialTheme.colorScheme.background
    val fg = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(52.dp))

        // Image area with badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1A3C34), Color(0xFF2D6A4F), Color(0xFF1A4A30))
                    )
                )
        ) {
            // Driver silhouette (decorative circles)
            DriverPlaceholderCanvas(modifier = Modifier.fillMaxSize())

            // Badge top-left
            GreenBadge(
                icon = Icons.Default.LocationOn,
                label = "LA\nCONFIANCE",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
            )
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Votre sécurité,\nnotre priorité.",
            color = fg,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 40.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        FeatureItem(
            icon = Icons.Default.Shield,
            title = "Chauffeurs certifiés",
            subtitle = "Vérification rigoureuse et continue."
        )
        Spacer(Modifier.height(16.dp))
        FeatureItem(
            icon = Icons.Default.LocationOn,
            title = "Suivi en temps réel",
            subtitle = "Partagez votre trajet avec vos proches."
        )
        Spacer(Modifier.height(16.dp))
        FeatureItem(
            icon = Icons.Default.Phone,
            title = "Assistance 24/7",
            subtitle = "Une équipe dédiée à votre écoute."
        )

        Spacer(Modifier.height(28.dp))

        DotsIndicator(totalDots = 4, selectedIndex = page)

        Spacer(Modifier.height(20.dp))

        OnboardingButton(label = "CONTINUER", onClick = onNext)

        Spacer(Modifier.height(16.dp))

        SkipLink(label = "Ignorer", onClick = onSkip)

        Spacer(Modifier.height(32.dp))
    }
}

// ─────────────────────────────────────────────────────────
// PAGE 3 — PAS DE MAUVAISES SURPRISES.
// ─────────────────────────────────────────────────────────

@Composable
private fun PagePrix(page: Int, onNext: () -> Unit, onSkip: () -> Unit) {
    val bg = MaterialTheme.colorScheme.background
    val fg = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(52.dp))

        // Receipt card
        ReceiptCard()

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Pas de mauvaises surprises.",
            color = fg,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 42.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Le prix est fixé avant même de commander. Vous savez exactement ce que vous payez, peu importe le trafic.",
            color = fg.copy(alpha = 0.6f),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(28.dp))

        DotsIndicator(totalDots = 4, selectedIndex = page)

        Spacer(Modifier.height(20.dp))

        OnboardingButton(label = "COMPRIS  →", onClick = onNext)

        Spacer(Modifier.height(16.dp))

        SkipLink(label = "Ignorer", onClick = onSkip)

        Spacer(Modifier.height(32.dp))
    }
}

// ─────────────────────────────────────────────────────────
// PAGE 4 — ON Y VA ?
// ─────────────────────────────────────────────────────────

@Composable
private fun PageLocalisation(onActivate: () -> Unit, onSkip: () -> Unit) {
    val bg = MaterialTheme.colorScheme.background
    val fg = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(28.dp))

        // Top bar: back + step label
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

        // Animated radar
        RadarAnimation(
            modifier = Modifier.size(220.dp)
        )

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

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Autorisez l'accès à votre position pour que nous puissions vous trouver rapidement.",
            color = fg.copy(alpha = 0.6f),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(40.dp))

        OnboardingButton(
            label = "ACTIVER LA LOCALISATION  ▶",
            onClick = onActivate
        )

        Spacer(Modifier.height(16.dp))

        SkipLink(label = "PLUS TARD", onClick = onSkip)

        Spacer(Modifier.height(32.dp))

        // Footer
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
}

// ─────────────────────────────────────────────────────────
// SHARED COMPONENTS
// ─────────────────────────────────────────────────────────

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
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(7.dp))
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
private fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalDots) { index ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(if (isSelected) 22.dp else 6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (isSelected) DadaDriveGreen
                        else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )
            )
        }
    }
}

@Composable
private fun GreenBadge(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(DadaDriveGreen, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(DadaDriveGreen.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DadaDriveGreen,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "DADA",
                        color = fg,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "le n° 8929-K.",
                        color = fg.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
                Box(
                    modifier = Modifier
                        .background(DadaDriveGreen, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
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

            ReceiptRow(label = "Trajet Standard", amount = "24.50€", fg = fg)
            Spacer(Modifier.height(8.dp))
            ReceiptRow(label = "Frais d'accès", amount = "0.00€", fg = fg)

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = fg.copy(alpha = 0.1f))
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL FIXÉ",
                    color = fg,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "24.50€",
                    color = fg,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(DadaDriveGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = DadaDriveGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "PRIX GARANTI",
                    color = DadaDriveGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, amount: String, fg: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = fg.copy(alpha = 0.6f), fontSize = 14.sp)
        Text(text = amount, color = fg, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun OnboardingButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DadaDriveGreen,
            contentColor = Color.White
        )
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
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
        modifier = Modifier.clickable { onClick() }
    )
}

// ─────────────────────────────────────────────────────────
// CANVAS VISUALS
// ─────────────────────────────────────────────────────────

@Composable
private fun RadarAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")

    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "s1"
    )
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "s2"
    )
    val scale3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "s3"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f

        listOf(scale1, scale2, scale3).forEach { s ->
            drawCircle(
                color = DadaDriveGreen.copy(alpha = (1f - s) * 0.5f),
                radius = maxR * s,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Static inner circle
        drawCircle(
            color = DadaDriveGreen.copy(alpha = 0.15f),
            radius = maxR * 0.25f,
            center = center
        )

        // Center pin dot
        drawCircle(
            color = DadaDriveGreen,
            radius = 10.dp.toPx(),
            center = center
        )
        // Pin stem
        drawLine(
            color = DadaDriveGreen,
            start = center,
            end = Offset(center.x, center.y + 18.dp.toPx()),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun SpeedLinesCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        listOf(0.2f, 0.4f, 0.6f, 0.75f, 0.88f).forEachIndexed { i, y ->
            val length = w * (0.3f + i * 0.1f)
            val startX = w * 0.1f + i * 10f
            drawLine(
                color = Color.White.copy(alpha = 0.06f + i * 0.02f),
                start = Offset(startX, h * y),
                end = Offset(startX + length, h * y),
                strokeWidth = (1.5f + i * 0.5f).dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun DriverPlaceholderCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        drawCircle(
            color = Color.White.copy(alpha = 0.05f),
            radius = size.minDimension * 0.38f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.03f),
            radius = size.minDimension * 0.55f,
            center = Offset(cx, cy)
        )
    }
}
