package com.qingshuige.tangyuan

import android.content.SharedPreferences
import com.qingshuige.tangyuan.TangyuanApplication.Companion.getSharedPreferences
import androidx.core.content.edit

class TokenManager {
    private val prefs: SharedPreferences

    init {
        prefs = getSharedPreferences()
    }

    var token: String?
        get() = prefs.getString("JwtToken", null)
        set(token) {
            prefs.edit { putString("JwtToken", token) }
        }

    val phoneNumber: String?
        get() = prefs.getString("phoneNumber", null)

    val password: String?
        get() = prefs.getString("password", null)

    fun setPhoneNumberAndPassword(phoneNumber: String?, password: String?) {
        prefs.edit {
            putString("phoneNumber", phoneNumber)
                .putString("password", password)
        }
    }
}
