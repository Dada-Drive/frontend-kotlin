package com.dadadrive.ui.role

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.dadadrive.R
import com.dadadrive.ui.theme.LocalAppColors

@Composable
fun RoleSelectionScreen(
    onSuccess: () -> Unit,
    viewModel: RoleViewModel = hiltViewModel()
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }
    val state by viewModel.state.collectAsState()
    var hasNavigatedOnSuccess by remember { mutableStateOf(false) }

    // Une seule navigation : évite les doubles appels si Success est re-lu (recomposition).
    LaunchedEffect(state) {
        if (state is RoleViewModel.RoleState.Success && !hasNavigatedOnSuccess) {
            hasNavigatedOnSuccess = true
            onSuccess()
        }
    }

    val c = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.darkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.role_selection_headline),
                color = c.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.role_selection_subtitle),
                color = c.textSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RoleCard(
                    title = stringResource(R.string.role_passenger_title),
                    subtitle = stringResource(R.string.role_passenger_subtitle),
                    icon = Icons.Default.Person,
                    role = "rider",
                    isSelected = selectedRole == "rider",
                    onSelect = { selectedRole = "rider" },
                    modifier = Modifier.weight(1f)
                )
                RoleCard(
                    title = stringResource(R.string.role_driver_title),
                    subtitle = stringResource(R.string.role_driver_subtitle),
                    icon = Icons.Default.Place,
                    role = "driver",
                    isSelected = selectedRole == "driver",
                    onSelect = { selectedRole = "driver" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.weight(1f))

            // Message d'erreur
            if (state is RoleViewModel.RoleState.Error) {
                Text(
                    text = (state as RoleViewModel.RoleState.Error).message,
                    color = c.errorRed,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            val isLoading = state is RoleViewModel.RoleState.Loading

            Button(
                onClick = { selectedRole?.let { viewModel.selectRole(it) } },
                enabled = selectedRole != null && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = c.primary,
                    disabledContainerColor = c.primary.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = c.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.role_continue),
                        color = c.onPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    role: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    val primary = c.primary
    val borderColor = if (isSelected) primary else c.surfaceMuted
    val bgColor = if (isSelected) primary.copy(alpha = 0.1f) else c.surfaceElevated

    Column(
        modifier = modifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .background(bgColor, RoundedCornerShape(20.dp))
            .clickable { onSelect() }
            .padding(vertical = 28.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = if (isSelected) primary else c.surfaceMuted,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) c.onPrimary else c.textPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = title,
            color = c.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = subtitle,
            color = c.textSecondary,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp
        )
    }
}
