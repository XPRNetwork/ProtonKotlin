package com.metallicus.protonsdk.common

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class Prefs(context: Context) {
	companion object {
		const val SHARED_PREFS_FILENAME = "protonsdk.prefs"

		const val API_KEY = "api_key"
		const val API_SECRET = "api_secret"

		const val HAS_CHAIN_PROVIDER = "has_chain_provider"
		const val HAS_TOKEN_CONTRACTS = "has_token_contracts"

		const val ACTIVE_CHAIN_ID = "active_chain_id"
		const val ACTIVE_ACCOUNT_NAME = "active_account_name"

		const val HAS_ACTIVE_ACCOUNT = "has_active_account"
	}

	//private val backupManager: BackupManager = BackupManager(context)
	private val prefs: SharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILENAME, 0)

	var apiKey: String
		get() = prefs.getString(API_KEY, "").orEmpty()
		set(value) {
			value.let {
				prefs.edit { putString(API_KEY, it) }
			}
		}

	var apiSecret: String
		get() = prefs.getString(API_SECRET, "").orEmpty()
		set(value) {
			value.let {
				prefs.edit { putString(API_SECRET, it) }
			}
		}

	var hasChainProvider: Boolean
		get() = prefs.getBoolean(HAS_CHAIN_PROVIDER, false)
		set(value) {
			value.let {
				prefs.edit { putBoolean(HAS_CHAIN_PROVIDER, it) }
			}
		}

	var hasTokenContracts: Boolean
		get() = prefs.getBoolean(HAS_TOKEN_CONTRACTS, false)
		set(value) {
			value.let {
				prefs.edit { putBoolean(HAS_TOKEN_CONTRACTS, it) }
			}
		}

	var activeChainId: String
		get() = prefs.getString(ACTIVE_CHAIN_ID, "").orEmpty()
		set(value) {
			value.let {
				prefs.edit { putString(ACTIVE_CHAIN_ID, it) }
			}
		}

	var activeAccountName: String
		get() = prefs.getString(ACTIVE_ACCOUNT_NAME, "").orEmpty()
		set(value) {
			value.let {
				prefs.edit { putString(ACTIVE_ACCOUNT_NAME, it) }
			}
		}

	var hasActiveAccount: Boolean
		get() = prefs.getBoolean(HAS_ACTIVE_ACCOUNT, false)
		set(value) {
			value.let {
				prefs.edit { putBoolean(HAS_ACTIVE_ACCOUNT, it) }
			}
		}

	fun clearInit() {
		prefs.edit {
			remove(HAS_CHAIN_PROVIDER)
			remove(HAS_TOKEN_CONTRACTS)
			remove(HAS_ACTIVE_ACCOUNT)
		}
	}

	fun clearAll() {
		clearInit()

		prefs.edit { remove(ACTIVE_CHAIN_ID) }

		prefs.edit { remove(ACTIVE_ACCOUNT_NAME) }

		//backupManager.dataChanged()
	}
}