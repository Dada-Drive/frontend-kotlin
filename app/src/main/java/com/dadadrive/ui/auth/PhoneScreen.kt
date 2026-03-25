package com.dadadrive.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dadadrive.ui.theme.DadaDriveGreen

// ─────────────────────────────────────────────────────────
// DATA
// ─────────────────────────────────────────────────────────

data class Country(val name: String, val flag: String, val dialCode: String)

val commonCountries = listOf(
    Country("France", "🇫🇷", "+33"),
    Country("Belgique", "🇧🇪", "+32"),
    Country("Suisse", "🇨🇭", "+41"),
    Country("Canada", "🇨🇦", "+1"),
    Country("Maroc", "🇲🇦", "+212"),
    Country("Algérie", "🇩🇿", "+213"),
    Country("Tunisie", "🇹🇳", "+216"),
    Country("Sénégal", "🇸🇳", "+221"),
    Country("Mali", "🇲🇱", "+223"),
    Country("Guinée", "🇬🇳", "+224"),
    Country("Côte d'Ivoire", "🇨🇮", "+225"),
    Country("Cameroun", "🇨🇲", "+237"),
    Country("Madagascar", "🇲🇬", "+261"),
    Country("Congo (RDC)", "🇨🇩", "+243"),
    Country("États-Unis", "🇺🇸", "+1"),
    Country("Royaume-Uni", "🇬🇧", "+44"),
    Country("Allemagne", "🇩🇪", "+49"),
    Country("Espagne", "🇪🇸", "+34"),
    Country("Italie", "🇮🇹", "+39"),
    Country("Portugal", "🇵🇹", "+351"),
    Country("Pays-Bas", "🇳🇱", "+31"),
    Country("Brésil", "🇧🇷", "+55"),
    Country("Nigeria", "🇳🇬", "+234"),
    Country("Ghana", "🇬🇭", "+233"),
)

// ─────────────────────────────────────────────────────────
// PHONE SCREEN
// ─────────────────────────────────────────────────────────

@Composable
fun PhoneScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    var phoneNumber by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(commonCountries.first()) }
    var showCountryPicker by remember { mutableStateOf(false) }

    val bg = MaterialTheme.colorScheme.background
    val fg = MaterialTheme.colorScheme.onBackground
    val isDark = isSystemInDarkTheme()
    val btnBg = if (isDark) Color.White else Color.Black
    val btnFg = if (isDark) Color.Black else Color.White
    val isLoading = authState is AuthState.Loading
    val canSubmit = phoneNumber.isNotBlank() && !isLoading

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onSuccess()
            authViewModel.resetState()
        }
    }

    if (showCountryPicker) {
        CountryPickerDialog(
            countries = commonCountries,
            onSelect = { country ->
                selectedCountry = country
                showCountryPicker = false
            },
            onDismiss = { showCountryPicker = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(bg)) {

        // ── Scrollable content ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 140.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = fg)
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Indiquez votre numéro",
                color = fg,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Nous vous enverrons un code par SMS pour sécuriser votre accès à l'expérience DadaDrive.",
                color = fg.copy(alpha = 0.6f),
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(40.dp))

            // ── Country + phone input row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Country selector (flag + code + chevron)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showCountryPicker = true }
                        .padding(vertical = 4.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedCountry.flag, fontSize = 24.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = selectedCountry.dialCode,
                        color = fg,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Changer de pays",
                        tint = fg.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Vertical divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(fg.copy(alpha = 0.2f))
                )

                Spacer(Modifier.width(12.dp))

                // Phone number field
                BasicTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    singleLine = true,
                    textStyle = TextStyle(color = fg, fontSize = 16.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    cursorBrush = SolidColor(DadaDriveGreen),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (phoneNumber.isEmpty()) {
                            Text("06 12 34 56 78", color = fg.copy(alpha = 0.3f), fontSize = 16.sp)
                        }
                        inner()
                    }
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = fg.copy(alpha = 0.15f))

            Spacer(Modifier.height(28.dp))

            // ── Privacy note ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = DadaDriveGreen,
                    modifier = Modifier.size(20.dp).padding(top = 2.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Votre vie privée est notre priorité. Votre numéro ne sera jamais partagé avec des tiers à des fins marketing.",
                    color = fg.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        // ── Fixed bottom: Continuer + step indicator ──
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
            Button(
                onClick = {
                    if (canSubmit) {
                        authViewModel.loginWithPhone(selectedCountry.dialCode + phoneNumber)
                    }
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = btnBg,
                    contentColor = btnFg,
                    disabledContainerColor = btnBg.copy(alpha = 0.35f),
                    disabledContentColor = btnFg.copy(alpha = 0.5f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Continuer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "ÉTAPE 1 SUR 6",
                color = fg.copy(alpha = 0.3f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────
// COUNTRY PICKER DIALOG
// ─────────────────────────────────────────────────────────

@Composable
private fun CountryPickerDialog(
    countries: List<Country>,
    onSelect: (Country) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) countries
        else countries.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val bg = MaterialTheme.colorScheme.surface
    val fg = MaterialTheme.colorScheme.onSurface

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .background(bg, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "Sélectionner un pays",
                color = fg,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            // Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = fg.copy(alpha = 0.07f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, null, tint = fg.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = TextStyle(color = fg, fontSize = 15.sp),
                    cursorBrush = SolidColor(DadaDriveGreen),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (searchQuery.isEmpty()) Text("Rechercher un pays...", color = fg.copy(alpha = 0.35f), fontSize = 15.sp)
                        inner()
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn {
                items(filtered) { country ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(country) }
                            .padding(vertical = 14.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(country.flag, fontSize = 24.sp)
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text = country.name,
                            color = fg,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = country.dialCode,
                            color = fg.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    HorizontalDivider(color = fg.copy(alpha = 0.07f))
                }
            }
        }
    }
}
