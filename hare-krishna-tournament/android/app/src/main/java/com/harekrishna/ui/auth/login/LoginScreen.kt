package com.harekrishna.ui.auth.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showPicker by remember { mutableStateOf(false) }

    // Tap detection on a readOnly TextField — opens the bottom sheet without
    // showing a keyboard or cursor.
    val pickerInteractionSource = remember { MutableInteractionSource() }
    LaunchedEffect(pickerInteractionSource) {
        pickerInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release && !state.isLoading && state.contestants.isNotEmpty()) {
                showPicker = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement  = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Hare Krishna",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "Sign in to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            // ── Bhakt name (opens BottomSheet on tap) ──
            OutlinedTextField(
                value             = state.bhaktName,
                onValueChange     = { /* read-only — picker is the source */ },
                readOnly          = true,
                label             = { Text("Bhakt Name") },
                placeholder       = { Text(if (state.isLoading) "Loading…" else "Select your name") },
                trailingIcon      = { Text("▾", style = MaterialTheme.typography.titleLarge) },
                enabled           = !state.isLoading,
                interactionSource = pickerInteractionSource,
                modifier          = Modifier.fillMaxWidth(),
            )

            // ── Password ──
            OutlinedTextField(
                value         = state.password,
                onValueChange = viewModel::onPasswordChange,
                label         = { Text("Password") },
                singleLine    = true,
                visualTransformation = if (state.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon  = {
                    TextButton(onClick = viewModel::onTogglePasswordVisibility) {
                        Text(
                            if (state.showPassword) "Hide" else "Show",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Error ──
            state.error?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            // ── Submit ──
            Button(
                onClick  = viewModel::onSubmit,
                enabled  = state.canSubmit,
                shape    = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier    = Modifier.size(20.dp),
                    )
                } else {
                    Text("Log in", style = MaterialTheme.typography.titleMedium)
                }
            }

            Text(
                "Forgot password? Contact admin.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // ── Modal picker for the bhaktName ──
        if (showPicker) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showPicker = false },
                sheetState       = sheetState,
            ) {
                Text(
                    "Choose your name",
                    style    = MaterialTheme.typography.titleMedium,
                    color    = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 12.dp),
                )
                HorizontalDivider()
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(state.contestants, key = { it.id }) { c ->
                        ListItem(
                            headlineContent = { Text(c.bhaktName) },
                            colors          = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                            modifier        = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onNameChange(c.bhaktName)
                                    showPicker = false
                                },
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
