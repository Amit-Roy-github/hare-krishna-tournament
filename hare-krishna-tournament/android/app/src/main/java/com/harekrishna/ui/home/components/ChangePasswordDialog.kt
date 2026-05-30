package com.harekrishna.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.harekrishna.domain.auth.AuthError
import kotlinx.coroutines.launch

private const val MIN_PASSWORD_LENGTH = 6

@Composable
fun ChangePasswordDialog(
    onSubmit:  suspend (current: String, new: String) -> Result<Unit>,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
) {
    var current   by remember { mutableStateOf("") }
    var newPwd    by remember { mutableStateOf("") }
    var confirm   by remember { mutableStateOf("") }
    var error     by remember { mutableStateOf<String?>(null) }
    var inFlight  by remember { mutableStateOf(false) }
    val scope     = rememberCoroutineScope()

    val canSubmit = !inFlight &&
        current.isNotEmpty() &&
        newPwd.length >= MIN_PASSWORD_LENGTH &&
        confirm == newPwd

    AlertDialog(
        onDismissRequest = { if (!inFlight) onDismiss() },
        title = { Text("Change password") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value                = current,
                    onValueChange        = { current = it; error = null },
                    label                = { Text("Current password") },
                    singleLine           = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier             = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value                = newPwd,
                    onValueChange        = { newPwd = it; error = null },
                    label                = { Text("New password (min $MIN_PASSWORD_LENGTH chars)") },
                    singleLine           = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier             = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value                = confirm,
                    onValueChange        = { confirm = it; error = null },
                    label                = { Text("Confirm new password") },
                    singleLine           = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError              = confirm.isNotEmpty() && confirm != newPwd,
                    modifier             = Modifier.fillMaxWidth(),
                )
                error?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSubmit,
                onClick = {
                    inFlight = true
                    scope.launch {
                        val r = onSubmit(current, newPwd)
                        inFlight = false
                        r.onSuccess { onSuccess() }
                         .onFailure { error = it.message() }
                    }
                },
            ) {
                Text(if (inFlight) "Saving…" else "Save")
            }
        },
        dismissButton = {
            TextButton(enabled = !inFlight, onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private fun Throwable.message(): String = when (this) {
    is AuthError.InvalidCredentials -> "Current password is incorrect"
    is AuthError.NoPasswordSet      -> "No password is set yet — ask admin"
    is AuthError.Network            -> "Network error — try again"
    is AuthError.Unknown            -> message ?: "Couldn't change password"
    else                            -> message ?: "Couldn't change password"
}
