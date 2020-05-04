package com.proton.protonchain.common

import android.app.backup.BackupManager
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class Prefs(context: Context) {
    companion object {
        const val PREFS_FILENAME = "protonchain.prefs"

        const val HAS_CHAIN_PROVIDERS = "has_chain_providers"
        const val HAS_TOKEN_CONTRACTS = "has_token_contracts"
        const val HAS_DAPPS = "has_dapps"
    }

    private val backupManager: BackupManager = BackupManager(context)
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    var hasChainProviders: Boolean
        get() = prefs.getBoolean(HAS_CHAIN_PROVIDERS, false)
        set(value) {
            value.let {
                prefs.edit { putBoolean(HAS_CHAIN_PROVIDERS, it) }
            }
        }

    var hasTokenContracts: Boolean
        get() = prefs.getBoolean(HAS_TOKEN_CONTRACTS, false)
        set(value) {
            value.let {
                prefs.edit { putBoolean(HAS_TOKEN_CONTRACTS, it) }
            }
        }

    var hasDApps: Boolean
        get() = prefs.getBoolean(HAS_DAPPS, false)
        set(value) {
            value.let {
                prefs.edit { putBoolean(HAS_DAPPS, it) }
            }
        }

    fun clearInit() {
        prefs.edit {
            remove(HAS_CHAIN_PROVIDERS)
            remove(HAS_TOKEN_CONTRACTS)
            remove(HAS_DAPPS)
        }
    }

    fun clearAll() {
        clearInit()

        backupManager.dataChanged()
    }
}