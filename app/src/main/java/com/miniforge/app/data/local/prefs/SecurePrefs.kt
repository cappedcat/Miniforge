package com.miniforge.app.data.local.prefs

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure storage for sensitive data like API keys using encrypted SharedPreferences.
 * Encryption/decryption is automatic — values are encrypted at rest.
 */
class SecurePrefs(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedSharedPrefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiKey(key: String, value: String) {
        encryptedSharedPrefs.edit().putString(key, value).apply()
    }

    fun getApiKey(key: String): String? {
        return encryptedSharedPrefs.getString(key, null)
    }

    fun deleteApiKey(key: String) {
        encryptedSharedPrefs.edit().remove(key).apply()
    }

    fun clearAllKeys() {
        encryptedSharedPrefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "secure_prefs"
    }
}
