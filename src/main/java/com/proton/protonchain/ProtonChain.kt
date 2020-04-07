package com.proton.protonchain

import com.proton.protonchain.securestorage.SecurePreferences

class ProtonChain {
    fun init() {
        SecurePreferences.clearAllValues()
    }
}