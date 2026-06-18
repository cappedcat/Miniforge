package com.miniforge.app.data.local.prefs

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePrefs @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "miniforge_secure",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun putApiKey(providerId: String, key: String) =
        prefs.edit { putString("apikey_$providerId", key) }

    fun getApiKey(providerId: String): String? =
        prefs.getString("apikey_$providerId", null)

    fun removeApiKey(providerId: String) =
        prefs.edit { remove("apikey_$providerId") }
}
