package com.harekrishna.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.harekrishna.domain.model.AuthSession
import com.harekrishna.domain.model.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Encrypted-at-rest session storage. Uses Jetpack Security's
// EncryptedSharedPreferences (AES-256-GCM values, AES-256-SIV keys) so the
// JWT never lives on disk in plaintext.
//
// `flow` is the reactive surface for AppNavigation — emits null when the
// session is cleared, so the UI re-routes to Login automatically.
class SessionPrefs(context: Context) {

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val _flow = MutableStateFlow(read())
    val flow: StateFlow<AuthSession?> = _flow.asStateFlow()

    fun read(): AuthSession? {
        val name  = prefs.getString(KEY_NAME,  null) ?: return null
        val role  = prefs.getString(KEY_ROLE,  null) ?: return null
        val token = prefs.getString(KEY_TOKEN, null) ?: return null
        return AuthSession(bhaktName = name, role = Role.fromWire(role), token = token)
    }

    fun save(session: AuthSession) {
        prefs.edit()
            .putString(KEY_NAME,  session.bhaktName)
            .putString(KEY_ROLE,  session.role.wire)
            .putString(KEY_TOKEN, session.token)
            .apply()
        _flow.value = session
    }

    fun clear() {
        prefs.edit().clear().apply()
        _flow.value = null
    }

    private companion object {
        const val FILE_NAME = "hkt_session"
        const val KEY_NAME  = "bhaktName"
        const val KEY_ROLE  = "role"
        const val KEY_TOKEN = "token"
    }
}
