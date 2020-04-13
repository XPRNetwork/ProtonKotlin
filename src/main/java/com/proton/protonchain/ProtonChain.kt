package com.proton.protonchain

import android.content.Context
import com.proton.protonchain.securestorage.SecurePreferences

class ProtonChain {
    fun init(context: Context) {
        SecurePreferences.clearAllValues(context)
    }
}