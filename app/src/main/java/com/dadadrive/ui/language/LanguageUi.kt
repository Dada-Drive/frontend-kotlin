package com.dadadrive.ui.language

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dadadrive.R
import com.dadadrive.core.language.AppLanguage

@Composable
fun AppLanguage.localizedDisplayName(): String = stringResource(
    when (this) {
        AppLanguage.SYSTEM -> R.string.lang_system
        AppLanguage.ENGLISH -> R.string.lang_english
        AppLanguage.FRENCH -> R.string.lang_french
        AppLanguage.ARABIC -> R.string.lang_arabic
    }
)
