// Équivalent Swift : Presentation/Auth/NameEntry/NameEntryView.swift
package com.dadadrive.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.dadadrive.R
import com.dadadrive.ui.theme.LocalAppColors

@Composable
fun NameEntryScreen(
    onContinue: () -> Unit,
    viewModel: NameEntryViewModel = hiltViewModel()
) {
    val c = LocalAppColors.current
    var name by remember { mutableStateOf("") }
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.darkSurface)
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            stringResource(R.string.name_entry_title),
            color = c.textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.name_entry_subtitle),
            color = c.textSecondary,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.auth_full_name), color = c.textSecondary) }
        )
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = c.errorRed, fontSize = 13.sp)
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { viewModel.submitFullName(name, onContinue) },
            enabled = !loading && name.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = c.primary)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(22.dp),
                    color = c.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.auth_continue), color = c.onPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
