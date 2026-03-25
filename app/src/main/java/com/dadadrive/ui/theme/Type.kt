package com.dadadrive.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DadaDriveTypography = Typography(
    headlineLarge = TextStyle(
        color = White,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        color = White,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    ),
    bodyLarge = TextStyle(
        color = White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        color = GreyLabel,
        fontSize = 14.sp
    ),
    labelSmall = TextStyle(
        color = GreyHint,
        fontSize = 12.sp
    )
)
