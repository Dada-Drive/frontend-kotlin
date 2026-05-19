@file:OptIn(ExperimentalMaterial3Api::class)

package tn.turbodrive.presentation.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbodrive.R
import tn.turbodrive.core.constants.Constants
import tn.turbodrive.core.designsystem.spacing.AppSpacing
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.theme.AppTypography
import tn.turbodrive.core.theme.LocalAppColors

// ─────────────────────────────────────────────────────────
// DATA
// ─────────────────────────────────────────────────────────

/**
 * [isoCode]      : code pays ISO (ex. TN)
 * [dialCode]     : indicatif international avec le +
 * [maxDigits]    : nombre de chiffres du numéro national (sans indicatif)
 * [formatMask]   : masque d'affichage — '#' = chiffre, ' ' = espace auto-inséré
 * [placeholder]  : exemple de numéro formaté affiché en hint
 */
data class Country(
    val isoCode: String,
    val name: String,
    val flag: String,
    val dialCode: String,
    val maxDigits: Int = 9,
    val formatMask: String = "### ### ###",
    val placeholder: String = "XXX XXX XXX",
)

val commonCountries =
    listOf(
        Country("TN", "Tunisie", "🇹🇳", "+216", 8, "## ### ###", "20 123 456"),
        Country("DZ", "Algérie", "🇩🇿", "+213", 9, "### ### ###", "551 234 567"),
        Country("MA", "Maroc", "🇲🇦", "+212", 9, "### ### ###", "612 345 678"),
        Country("FR", "France", "🇫🇷", "+33", 9, "# ## ## ## ##", "6 12 34 56 78"),
        Country("IT", "Italie", "🇮🇹", "+39", 10, "### ### ####", "312 345 6789"),
        Country("EG", "Égypte", "🇪🇬", "+20", 10, "### ### ####", "100 123 4567"),
        Country("LY", "Libye", "🇱🇾", "+218", 9, "## ### ####", "91 234 5678"),
        Country("BE", "Belgique", "🇧🇪", "+32", 9, "### ## ## ##", "470 12 34 56"),
        Country("CH", "Suisse", "🇨🇭", "+41", 9, "## ### ## ##", "76 123 45 67"),
        Country("CA", "Canada", "🇨🇦", "+1", 10, "### ### ####", "514 123 4567"),
        Country("SN", "Sénégal", "🇸🇳", "+221", 9, "## ### ## ##", "70 123 45 67"),
        Country("ML", "Mali", "🇲🇱", "+223", 8, "## ## ## ##", "70 12 34 56"),
        Country("GN", "Guinée", "🇬🇳", "+224", 9, "### ## ## ##", "620 12 34 56"),
        Country("CI", "Côte d'Ivoire", "🇨🇮", "+225", 10, "## ## ## ## ##", "07 12 34 56 78"),
        Country("CM", "Cameroun", "🇨🇲", "+237", 9, "### ### ###", "676 123 456"),
        Country("MG", "Madagascar", "🇲🇬", "+261", 9, "## ## ### ##", "32 12 345 67"),
        Country("CD", "Congo (RDC)", "🇨🇩", "+243", 9, "### ### ###", "812 345 678"),
        Country("US", "États-Unis", "🇺🇸", "+1", 10, "### ### ####", "212 555 1234"),
        Country("GB", "Royaume-Uni", "🇬🇧", "+44", 10, "#### ### ####", "7911 123 456"),
        Country("DE", "Allemagne", "🇩🇪", "+49", 11, "#### ## ## ###", "1511 12 34 567"),
        Country("ES", "Espagne", "🇪🇸", "+34", 9, "### ### ###", "612 345 678"),
        Country("PT", "Portugal", "🇵🇹", "+351", 9, "### ### ###", "912 345 678"),
        Country("NL", "Pays-Bas", "🇳🇱", "+31", 9, "# #### ####", "6 1234 5678"),
        Country("BR", "Brésil", "🇧🇷", "+55", 11, "## ##### ####", "11 91234 5678"),
        Country("NG", "Nigeria", "🇳🇬", "+234", 10, "### ### ####", "802 123 4567"),
        Country("GH", "Ghana", "🇬🇭", "+233", 9, "## ### ####", "20 123 4567"),
    )

/** Applique le masque de formatage sur les chiffres bruts */
fun applyMask(
    rawDigits: String,
    mask: String,
): String {
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

/** Affiche le masque sans altérer la valeur réelle (chiffres seuls) — le curseur reste cohérent. */
private class PhoneMaskVisualTransformation(private val mask: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val formatted = applyMask(raw, mask)
        val mapping =
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset <= 0) return 0
                    val o = offset.coerceIn(0, raw.length)
                    return applyMask(raw.take(o), mask).length
                }

                override fun transformedToOriginal(offset: Int): Int {
                    if (raw.isEmpty()) return 0
                    val visual = applyMask(raw, mask)
                    val coerced = offset.coerceIn(0, visual.length)
                    return visual.take(coerced).count { it.isDigit() }
                }
            }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}

private fun defaultPhoneCountry(): Country = commonCountries.find { it.dialCode == "+216" } ?: commonCountries.first()

/**
 * S04 privacy note (screens-auth.jsx:222) : "<b>Confidentialité garantie.</b> ..."
 * Bolds the first sentence (up to and including the first period) for the visual
 * emphasis specified in the redesign. Falls back to plain text if no period found
 * (defensive — should not happen with the three locale strings ar/fr/en).
 */
private fun privacyNoteAnnotated(raw: String): AnnotatedString =
    buildAnnotatedString {
        val periodIndex = raw.indexOf('.')
        if (periodIndex < 0) {
            append(raw)
        } else {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(raw.substring(0, periodIndex + 1))
            }
            append(raw.substring(periodIndex + 1))
        }
    }

// ─────────────────────────────────────────────────────────
// PHONE SCREEN
// ─────────────────────────────────────────────────────────

@Composable
fun PhoneScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onOtpVerified: () -> Unit,
) {
    val otpState by authViewModel.otpState.collectAsState()
    val resendCooldown by authViewModel.resendCooldown.collectAsState()
    var rawDigits by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(defaultPhoneCountry()) }
    var showCountryPicker by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var otpFlowPhone by remember { mutableStateOf<String?>(null) }
    var prevOtpLen by remember { mutableIntStateOf(0) }

    val phoneVisualTransformation =
        remember(selectedCountry.formatMask) {
            PhoneMaskVisualTransformation(selectedCountry.formatMask)
        }

    val appColors = LocalAppColors.current
    val creamBg = appColors.background
    val ink = Color.Black
    val muted = appColors.textSecondary
    val bg = creamBg
    val fg = ink
    val isDarkUi = false
    var showLocalPhoneError by remember { mutableStateOf(false) }

    val fullPhone = "${selectedCountry.dialCode}$rawDigits"
    val sendingOtp = otpState is OtpUiState.SendingOtp
    val verifyingOtp = otpState is OtpUiState.VerifyingOtp
    val phase1Error = (otpState as? OtpUiState.Error)?.takeIf { otpFlowPhone == null }?.message
    val phase2Error = (otpState as? OtpUiState.Error)?.takeIf { otpFlowPhone != null }?.message

    val showOtpPhase = otpFlowPhone != null

    OtpAutofillEffects(
        enabled = showOtpPhase && otpCode.length < Constants.PHONE_CODE_LENGTH,
        sessionKey = otpFlowPhone,
        onOtpFilled = { otpCode = it },
    )

    val countrySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(selectedCountry) {
        rawDigits = ""
    }

    LaunchedEffect(rawDigits) {
        showLocalPhoneError = false
    }

    LaunchedEffect(otpState) {
        when (val s = otpState) {
            is OtpUiState.OtpSent -> otpFlowPhone = s.phone
            is OtpUiState.VerifyingOtp -> otpFlowPhone = s.phone
            is OtpUiState.Idle -> {
                otpFlowPhone = null
                otpCode = ""
            }
            is OtpUiState.Success -> otpFlowPhone = null
            else -> {}
        }
    }

    LaunchedEffect(otpState) {
        if (otpState is OtpUiState.Success) {
            onOtpVerified()
            authViewModel.resetOtpState()
            otpCode = ""
        }
    }

    LaunchedEffect(showOtpPhase) {
        if (!showOtpPhase) {
            otpCode = ""
            prevOtpLen = 0
        }
    }

    LaunchedEffect(otpCode, otpFlowPhone, otpState) {
        val phone = otpFlowPhone ?: return@LaunchedEffect
        if (otpCode.length != Constants.PHONE_CODE_LENGTH) {
            prevOtpLen = otpCode.length
            return@LaunchedEffect
        }
        if (otpState is OtpUiState.VerifyingOtp || otpState is OtpUiState.Success) return@LaunchedEffect
        if (otpState is OtpUiState.SendingOtp) return@LaunchedEffect
        val shouldSubmit =
            prevOtpLen < Constants.PHONE_CODE_LENGTH &&
                (otpState is OtpUiState.OtpSent || otpState is OtpUiState.Error)
        prevOtpLen = otpCode.length
        if (shouldSubmit) {
            authViewModel.verifyOtp(phone, otpCode)
        }
    }

    if (showCountryPicker) {
        ModalBottomSheet(
            onDismissRequest = { showCountryPicker = false },
            sheetState = countrySheetState,
            containerColor = appColors.surface,
            dragHandle = {
                Box(
                    Modifier
                        .padding(top = 12.dp, bottom = 4.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .background(appColors.dragHandle, CircleShape),
                )
            },
        ) {
            CountryPickerSheetContent(
                countries = commonCountries,
                selected = selectedCountry,
                onDismiss = { showCountryPicker = false },
                onSelect = { country ->
                    selectedCountry = country
                    showCountryPicker = false
                },
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        AnimatedContent(
            targetState = showOtpPhase,
            transitionSpec = {
                (
                    slideInVertically(
                        animationSpec = tween(320),
                        initialOffsetY = { it },
                    ) + fadeIn(tween(280))
                ) togetherWith
                    (
                        slideOutVertically(
                            animationSpec = tween(280),
                            targetOffsetY = { -it / 2 },
                        ) + fadeOut(tween(200))
                    )
            },
            label = "phone_otp_phase",
        ) { otpPhase ->
            if (!otpPhase) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(bottom = 8.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                ) {
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth()) {
                        Box(
                            modifier =
                                Modifier
                                    .align(Alignment.CenterStart)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(appColors.surface)
                                    .border(1.dp, appColors.border, CircleShape)
                                    .clickable { onBack() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(AppIcon.arrowLeft),
                                contentDescription = stringResource(R.string.cd_back),
                                tint = fg,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Text(
                            text = stringResource(R.string.auth_registration_step, 1, 4),
                            modifier = Modifier.align(Alignment.Center),
                            color = fg,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(28.dp))
                    Text(
                        text = stringResource(R.string.phone_title),
                        color = fg,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.phone_subtitle),
                        color = muted,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                    )
                    Spacer(Modifier.height(28.dp))
                    Text(
                        text = stringResource(R.string.phone_field_label),
                        color = fg,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    val phoneBorderColor =
                        when {
                            phase1Error != null || showLocalPhoneError -> appColors.error
                            else -> appColors.inkSoft
                        }
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(appColors.surface)
                                .border(1.5.dp, phoneBorderColor, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showCountryPicker = true }
                                    .padding(vertical = 2.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${selectedCountry.isoCode} ${selectedCountry.dialCode}",
                                color = fg,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                painter = painterResource(AppIcon.chevronDown),
                                contentDescription = stringResource(R.string.cd_change_country),
                                tint = muted,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Box(
                            modifier =
                                Modifier
                                    .padding(horizontal = 10.dp)
                                    .width(1.dp)
                                    .height(26.dp)
                                    .background(appColors.border),
                        )
                        BasicTextField(
                            value = rawDigits,
                            onValueChange = { input ->
                                val digits = input.filter { it.isDigit() }
                                if (digits.length <= selectedCountry.maxDigits) {
                                    rawDigits = digits
                                }
                            },
                            visualTransformation = phoneVisualTransformation,
                            singleLine = true,
                            textStyle =
                                TextStyle(
                                    color = if (showLocalPhoneError) appColors.error else fg,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            cursorBrush = SolidColor(Color.Black),
                            modifier = Modifier.weight(1f),
                            decorationBox = { inner ->
                                if (rawDigits.isEmpty()) {
                                    Text(
                                        selectedCountry.placeholder,
                                        color = appColors.borderStrong,
                                        fontSize = 16.sp,
                                    )
                                }
                                inner()
                            },
                        )
                    }
                    if (showLocalPhoneError) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text =
                                stringResource(
                                    R.string.phone_invalid_digits,
                                    selectedCountry.maxDigits,
                                ),
                            color = appColors.error,
                            fontSize = 13.sp,
                        )
                    }
                    if (phase1Error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = phase1Error,
                            color = appColors.error,
                            fontSize = 13.sp,
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(appColors.accentSoft)
                                .padding(14.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            painter = painterResource(AppIcon.lock),
                            contentDescription = null,
                            tint = appColors.accentInk,
                            modifier = Modifier.size(20.dp).padding(top = 2.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = privacyNoteAnnotated(stringResource(R.string.phone_privacy_note)),
                            color = appColors.accentInk,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }
            } else {
                val otpPhoneDisplay = otpFlowPhone.orEmpty()
                val otpErr = phase2Error != null
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp),
                ) {
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth()) {
                        Box(
                            modifier =
                                Modifier
                                    .align(Alignment.CenterStart)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(appColors.surface)
                                    .border(1.dp, appColors.border, CircleShape)
                                    .clickable {
                                        otpCode = ""
                                        authViewModel.resetOtpState()
                                    },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(AppIcon.arrowLeft),
                                contentDescription = stringResource(R.string.cd_back),
                                tint = fg,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Text(
                            text = stringResource(R.string.auth_registration_step, 2, 4),
                            modifier = Modifier.align(Alignment.Center),
                            color = fg,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.otp_title_whatsapp),
                        color = fg,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.otp_instruction_prefix),
                        color = muted,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = otpPhoneDisplay,
                        color = fg,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(appColors.accentSoft)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_whatsapp),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.otp_sent_via_whatsapp),
                            color = appColors.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    val otpBoxWidth = 52.dp
                    val otpBoxHeight = 60.dp
                    val otpGap = 10.dp
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(otpBoxHeight),
                    ) {
                        BasicTextField(
                            value = otpCode,
                            onValueChange = { input ->
                                val digits = input.filter { it.isDigit() }
                                if (digits.length <= Constants.PHONE_CODE_LENGTH) {
                                    otpCode = digits
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            cursorBrush = SolidColor(Color.Transparent),
                            textStyle = TextStyle(color = Color.Transparent, fontSize = 1.sp),
                            singleLine = true,
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .alpha(0f),
                        )
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Center),
                            horizontalArrangement = Arrangement.spacedBy(otpGap, Alignment.CenterHorizontally),
                        ) {
                            repeat(Constants.PHONE_CODE_LENGTH) { index ->
                                val digit = otpCode.getOrNull(index)?.toString().orEmpty()
                                val isFocused = index == otpCode.length
                                val isFilled = digit.isNotEmpty()
                                val borderColor =
                                    when {
                                        otpErr -> appColors.error
                                        isFilled || isFocused -> Color.Black
                                        else -> appColors.border
                                    }
                                Box(
                                    modifier =
                                        Modifier
                                            .width(otpBoxWidth)
                                            .height(otpBoxHeight)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(appColors.surface)
                                            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isFilled) {
                                        Text(
                                            text = digit,
                                            color = if (otpErr) appColors.error else fg,
                                            style = AppTypography.monoM,
                                            textAlign = TextAlign.Center,
                                        )
                                    } else if (isFocused) {
                                        Text(
                                            text = "|",
                                            color = fg.copy(alpha = 0.35f),
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Light,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    AnimatedVisibility(visible = otpErr, enter = fadeIn(), exit = fadeOut()) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(appColors.errorSoft)
                                    .border(1.dp, appColors.errorSoft, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("!", color = appColors.error, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = phase2Error.orEmpty(),
                                color = appColors.error,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    val mm = resendCooldown / 60
                    val ss = resendCooldown % 60
                    if (resendCooldown > 0) {
                        Text(
                            text = stringResource(R.string.otp_resend_timer, mm, ss),
                            color = muted,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        TextButton(
                            onClick = {
                                otpCode = ""
                                prevOtpLen = 0
                                otpFlowPhone?.let { authViewModel.sendOtp(it) }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                stringResource(R.string.otp_resend),
                                color = appColors.accent,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    OtpNumericKeypad(
                        onDigit = { d ->
                            if (otpCode.length < Constants.PHONE_CODE_LENGTH) {
                                otpCode += d.toString()
                            }
                        },
                        onBackspace = {
                            if (otpCode.isNotEmpty()) otpCode = otpCode.dropLast(1)
                        },
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(bg.copy(alpha = 0f), bg, bg),
                            startY = 0f,
                            endY = 80f,
                        ),
                    )
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp, top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!showOtpPhase) {
                Button(
                    onClick = {
                        if (rawDigits.length < selectedCountry.maxDigits) {
                            showLocalPhoneError = true
                        } else {
                            showLocalPhoneError = false
                            authViewModel.sendOtp(fullPhone)
                        }
                    },
                    enabled = !sendingOtp,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White,
                            disabledContainerColor = appColors.borderStrong,
                            disabledContentColor = Color.White,
                        ),
                ) {
                    if (sendingOtp) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_whatsapp),
                                contentDescription = stringResource(R.string.cd_whatsapp),
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                stringResource(R.string.phone_whatsapp_cta),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.phone_sms_disclaimer),
                    color = muted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
            } else {
                val p = otpFlowPhone
                Button(
                    onClick = {
                        if (p != null &&
                            otpCode.length == Constants.PHONE_CODE_LENGTH &&
                            otpState !is OtpUiState.VerifyingOtp
                        ) {
                            authViewModel.verifyOtp(p, otpCode)
                        }
                    },
                    enabled = otpCode.length == Constants.PHONE_CODE_LENGTH && !verifyingOtp,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White,
                            disabledContainerColor = appColors.border,
                            disabledContentColor = appColors.textSubtle,
                        ),
                ) {
                    if (verifyingOtp) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            stringResource(R.string.otp_verify_cta),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OtpNumericKeypad(
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
) {
    val rows: List<List<Any?>> =
        listOf(
            listOf(1, 2, 3),
            listOf(4, 5, 6),
            listOf(7, 8, 9),
            listOf(null, 0, "del"),
        )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            ) {
                row.forEach { cell ->
                    when {
                        cell == null -> Spacer(Modifier.width(76.dp).height(48.dp))
                        cell == "del" ->
                            OtpKeypadKey(onClick = onBackspace) {
                                Icon(
                                    painter = painterResource(AppIcon.arrowLeft),
                                    contentDescription = stringResource(R.string.cd_backspace),
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        cell is Int ->
                            OtpKeypadKey(onClick = { onDigit(cell) }) {
                                Text(
                                    text = cell.toString(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                )
                            }
                    }
                }
            }
        }
    }
}

@Composable
private fun OtpKeypadKey(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val appColors = LocalAppColors.current
    Box(
        modifier =
            Modifier
                .size(width = 76.dp, height = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(appColors.surface)
                .border(1.dp, appColors.border, RoundedCornerShape(12.dp))
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

// ─────────────────────────────────────────────────────────
// COUNTRY PICKER (ModalBottomSheet — design-system.md §7.11)
// ─────────────────────────────────────────────────────────

@Composable
private fun CountryPickerSheetContent(
    countries: List<Country>,
    selected: Country,
    onDismiss: () -> Unit,
    onSelect: (Country) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered =
        remember(searchQuery, countries) {
            if (searchQuery.isBlank()) {
                countries
            } else {
                countries.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                        it.isoCode.contains(searchQuery, ignoreCase = true) ||
                        it.dialCode.contains(searchQuery, ignoreCase = true)
                }
            }
        }

    val c = LocalAppColors.current
    val fg = c.textPrimary
    val fgMuted = c.textSecondary

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = AppSpacing.l, vertical = AppSpacing.s),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.country_picker_title),
                color = fg,
                style = AppTypography.headingM,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    painter = painterResource(AppIcon.close),
                    contentDescription = stringResource(R.string.cd_close),
                    tint = fg,
                )
            }
        }

        Spacer(Modifier.height(AppSpacing.m))

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppSpacing.inputRadius))
                    .background(c.surfaceAlt)
                    .padding(horizontal = AppSpacing.m, vertical = AppSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(AppIcon.search),
                contentDescription = null,
                tint = c.textSubtle,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(AppSpacing.s))
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                textStyle = AppTypography.bodyM.copy(color = fg),
                cursorBrush = SolidColor(c.primary),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (searchQuery.isEmpty()) {
                        Text(
                            stringResource(R.string.country_picker_search_hint),
                            color = c.textSubtle,
                            style = AppTypography.bodyM,
                        )
                    }
                    inner()
                },
            )
        }

        Spacer(Modifier.height(AppSpacing.m))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(filtered) { country ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(country)
                            }
                            .padding(vertical = AppSpacing.m, horizontal = AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = country.isoCode,
                        color = fg,
                        style = AppTypography.labelM,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(36.dp),
                    )
                    Text(
                        text = country.name,
                        color = fg,
                        style = AppTypography.bodyM,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = country.dialCode,
                        color = fgMuted,
                        style = AppTypography.bodyM,
                    )
                    if (country.isoCode == selected.isoCode) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(AppIcon.check),
                            contentDescription = stringResource(R.string.cd_selected),
                            tint = c.accent,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                HorizontalDivider(color = c.divider)
            }
        }
    }
}
