package com.dadadrive.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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

/**
 * [dialCode]     : indicatif international avec le +
 * [maxDigits]    : nombre de chiffres du numéro national (sans indicatif)
 * [formatMask]   : masque d'affichage — '#' = chiffre, ' ' = espace auto-inséré
 * [placeholder]  : exemple de numéro formaté affiché en hint
 */
data class Country(
    val name: String,
    val flag: String,
    val dialCode: String,
    val maxDigits: Int = 9,
    val formatMask: String = "### ### ###",
    val placeholder: String = "XXX XXX XXX"
)

val commonCountries = listOf(
    Country("France",        "🇫🇷", "+33",  9,  "# ## ## ## ##", "6 12 34 56 78"),
    Country("Belgique",      "🇧🇪", "+32",  9,  "### ## ## ##",  "470 12 34 56"),
    Country("Suisse",        "🇨🇭", "+41",  9,  "## ### ## ##",  "76 123 45 67"),
    Country("Canada",        "🇨🇦", "+1",   10, "### ### ####",  "514 123 4567"),
    Country("Maroc",         "🇲🇦", "+212", 9,  "### ### ###",   "612 345 678"),
    Country("Algérie",       "🇩🇿", "+213", 9,  "### ### ###",   "551 234 567"),
    Country("Tunisie",       "🇹🇳", "+216", 8,  "## ### ###",    "20 123 456"),
    Country("Sénégal",       "🇸🇳", "+221", 9,  "## ### ## ##",  "70 123 45 67"),
    Country("Mali",          "🇲🇱", "+223", 8,  "## ## ## ##",   "70 12 34 56"),
    Country("Guinée",        "🇬🇳", "+224", 9,  "### ## ## ##",  "620 12 34 56"),
    Country("Côte d'Ivoire", "🇨🇮", "+225", 10, "## ## ## ## ##","07 12 34 56 78"),
    Country("Cameroun",      "🇨🇲", "+237", 9,  "### ### ###",   "676 123 456"),
    Country("Madagascar",    "🇲🇬", "+261", 9,  "## ## ### ##",  "32 12 345 67"),
    Country("Congo (RDC)",   "🇨🇩", "+243", 9,  "### ### ###",   "812 345 678"),
    Country("États-Unis",    "🇺🇸", "+1",   10, "### ### ####",  "212 555 1234"),
    Country("Royaume-Uni",   "🇬🇧", "+44",  10, "#### ### ####", "7911 123 456"),
    Country("Allemagne",     "🇩🇪", "+49",  11, "#### ## ## ###","1511 12 34 567"),
    Country("Espagne",       "🇪🇸", "+34",  9,  "### ### ###",   "612 345 678"),
    Country("Italie",        "🇮🇹", "+39",  10, "### ### ####",  "312 345 6789"),
    Country("Portugal",      "🇵🇹", "+351", 9,  "### ### ###",   "912 345 678"),
    Country("Pays-Bas",      "🇳🇱", "+31",  9,  "# #### ####",  "6 1234 5678"),
    Country("Brésil",        "🇧🇷", "+55",  11, "## ##### ####", "11 91234 5678"),
    Country("Nigeria",       "🇳🇬", "+234", 10, "### ### ####",  "802 123 4567"),
    Country("Ghana",         "🇬🇭", "+233", 9,  "## ### ####",   "20 123 4567"),
)

/** Applique le masque de formatage sur les chiffres bruts */
fun applyMask(rawDigits: String, mask: String): String {
    val result = StringBuilder()
    var digitIndex = 0
    for (ch in mask) {
        if (digitIndex >= rawDigits.length) break
        if (ch == '#') {
            result.append(rawDigits[digitIndex++])
        } else {
            if (digitIndex > 0) result.append(ch)
        }
    }
    return result.toString()
}

// ─────────────────────────────────────────────────────────
// PHONE SCREEN
// ─────────────────────────────────────────────────────────

@Composable
fun PhoneScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onSuccess: (fullPhone: String) -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    var rawDigits by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(commonCountries.first()) }
    var showCountryPicker by remember { mutableStateOf(false) }

    val bg = MaterialTheme.colorScheme.background
    val fg = MaterialTheme.colorScheme.onBackground
    val isDark = isSystemInDarkTheme()
    val btnBg = if (isDark) Color.White else Color.Black
    val btnFg = if (isDark) Color.Black else Color.White
    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    val displayText = applyMask(rawDigits, selectedCountry.formatMask)
    val fullPhone = "${selectedCountry.dialCode}${rawDigits}"
    val canSubmit = rawDigits.length >= (selectedCountry.maxDigits - 1) && !isLoading

    // Quand le pays change, vider le numéro pour éviter un format invalide
    LaunchedEffect(selectedCountry) {
        rawDigits = ""
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.OtpSent) {
            authViewModel.resetState()
            onSuccess(fullPhone)
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
                .padding(bottom = 160.dp)
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

            // ── Sélecteur de pays + champ de saisie ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flag + indicatif + chevron
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

                // Séparateur vertical
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(fg.copy(alpha = 0.2f))
                )

                Spacer(Modifier.width(12.dp))

                // Champ de saisie — capture les chiffres bruts, affiche le format
                BasicTextField(
                    value = displayText,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= selectedCountry.maxDigits) {
                            rawDigits = digits
                        }
                    },
                    singleLine = true,
                    textStyle = TextStyle(color = fg, fontSize = 16.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    cursorBrush = SolidColor(DadaDriveGreen),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (rawDigits.isEmpty()) {
                            Text(
                                selectedCountry.placeholder,
                                color = fg.copy(alpha = 0.3f),
                                fontSize = 16.sp
                            )
                        }
                        inner()
                    }
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(
                color = if (rawDigits.isNotEmpty()) DadaDriveGreen.copy(alpha = 0.6f)
                        else fg.copy(alpha = 0.15f)
            )

            // ── Message d'erreur ──
            if (errorMessage != null) {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEDED), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFB00020),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Note de confidentialité ──
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

        // ── Bouton fixe en bas ──
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
                        authViewModel.resetState()
                        authViewModel.sendOtp(fullPhone)
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
                        color = if (isDark) Color.Black else Color.White,
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

            // Barre de recherche
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(fg.copy(alpha = 0.07f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search, null,
                    tint = fg.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = TextStyle(color = fg, fontSize = 15.sp),
                    cursorBrush = SolidColor(DadaDriveGreen),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                "Rechercher un pays...",
                                color = fg.copy(alpha = 0.35f),
                                fontSize = 15.sp
                            )
                        }
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(country.name, color = fg, fontSize = 15.sp)
                            Text(
                                text = country.placeholder,
                                color = fg.copy(alpha = 0.4f),
                                fontSize = 12.sp
                            )
                        }
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
