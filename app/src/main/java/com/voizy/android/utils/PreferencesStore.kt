package com.voizy.android.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesStore(
    val context: Context
) {

    companion object {
        private const val USER_PREFERENCES: String = "user_preferences"
        // Add different one for application preferences
    }

    val userTermsAgreed = BooleanStoreItem(USER_PREFERENCES, "user_terms_agreed", context)

    fun clear() {
        getSharedPreferences(USER_PREFERENCES)
    }

    private fun getSharedPreferences(cacheName: String): SharedPreferences {
        return context.getSharedPreferences(cacheName, Context.MODE_PRIVATE)
    }
}