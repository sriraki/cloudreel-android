package com.cloudreel.mobile.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.cloudreel.mobile.data.model.User
import com.cloudreel.mobile.data.model.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback for emulators/older OS without keystore hardware backed
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _isAuthenticated = MutableStateFlow(isSessionActive())
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    fun isSessionActive(): Boolean {
        val token = prefs.getString(KEY_SESSION_TOKEN, null)
        val loginTime = prefs.getLong(KEY_LOGIN_TIME, 0L)
        if (token.isNullOrBlank()) return false

        // Corporate session duration: 30 days
        val isExpired = (System.currentTimeMillis() - loginTime) > SESSION_VALIDITY_MS
        return !isExpired
    }

    fun saveSession(userData: UserData) {
        prefs.edit()
            .putString(KEY_SESSION_TOKEN, userData.sessionToken)
            .putString(KEY_USER_ID, userData.id)
            .putString(KEY_USER_EMAIL, userData.email)
            .putString(KEY_USERNAME, userData.username)
            .putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            .apply()

        _isAuthenticated.value = true
    }

    fun getUser(): User? {
        val token = prefs.getString(KEY_SESSION_TOKEN, null) ?: return null
        val id = prefs.getString(KEY_USER_ID, "") ?: ""
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val username = prefs.getString(KEY_USERNAME, "") ?: ""

        return User(
            id = id,
            email = email,
            username = username,
            sessionToken = token
        )
    }

    fun getSessionToken(): String? {
        return prefs.getString(KEY_SESSION_TOKEN, null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        _isAuthenticated.value = false
    }

    companion object {
        private const val PREFS_NAME = "cloudreel_secure_prefs"
        private const val KEY_SESSION_TOKEN = "pcr_session_token"
        private const val KEY_USER_ID = "pcr_user_id"
        private const val KEY_USER_EMAIL = "pcr_user_email"
        private const val KEY_USERNAME = "pcr_username"
        private const val KEY_LOGIN_TIME = "pcr_login_timestamp"

        private const val SESSION_VALIDITY_MS = 30L * 24 * 60 * 60 * 1000 // 30 days
    }
}
