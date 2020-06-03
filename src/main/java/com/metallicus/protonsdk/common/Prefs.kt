package com.metallicus.protonsdk.common

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class Prefs(context: Context) {
	companion object {
		const val SHARED_PREFS_FILENAME = "protonsdk.prefs"

		const val HAS_CHAIN_PROVIDERS = "has_chain_providers"
		const val HAS_TOKEN_CONTRACTS = "has_token_contracts"
		const val HAS_DAPPS = "has_dapps"

		const val SELECTED_ACCOUNT_CHAIN_ID = "selected_account_chain_id"
		const val SELECTED_ACCOUNT_NAME = "selected_account_name"
	}

	//private val backupManager: BackupManager = BackupManager(context)
	private val prefs: SharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILENAME, 0)

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

	var selectedAccountChainId: String?
		get() = prefs.getString(SELECTED_ACCOUNT_CHAIN_ID, "")
		set(value) {
			value?.let {
				prefs.edit { putString(SELECTED_ACCOUNT_CHAIN_ID, it) }
			}
		}

	var selectedAccountName: String?
		get() = prefs.getString(SELECTED_ACCOUNT_NAME, "")
		set(value) {
			value?.let {
				prefs.edit { putString(SELECTED_ACCOUNT_NAME, it) }
			}
		}

	fun clearSelectedAccount() {
		prefs.edit { remove(SELECTED_ACCOUNT_CHAIN_ID) }
		prefs.edit { remove(SELECTED_ACCOUNT_NAME) }
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
		clearSelectedAccount()

		//backupManager.dataChanged()
	}
}