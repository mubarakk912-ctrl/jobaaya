package com.example.data.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "jobaaya_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _isLoggedIn = MutableStateFlow(sharedPreferences.getString(KEY_AUTH_TOKEN, null) != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
        _isLoggedIn.value = true
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    fun saveUserData(userId: String, email: String?, mobile: String?) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_MOBILE, mobile)
        }.apply()
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
        _isLoggedIn.value = false
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_MOBILE = "user_mobile"
    }
}
