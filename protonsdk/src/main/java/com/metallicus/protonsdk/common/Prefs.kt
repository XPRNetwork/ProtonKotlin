/*
 * Copyright (c) 2020 Proton Chain LLC, Delaware
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.metallicus.protonsdk.common

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class Prefs(context: Context) {
	companion object {
		const val SHARED_PREFS_FILENAME = "protonsdk.prefs"

		const val HAS_CHAIN_PROVIDER = "has_chain_provider"
		const val HAS_TOKEN_CONTRACTS = "has_token_contracts"

		const val ACTIVE_CHAIN_ID = "active_chain_id"
		const val ACTIVE_ACCOUNT_NAME = "active_account_name"

		const val HAS_ACTIVE_ACCOUNT = "has_active_account"
	}

	//private val backupManager: BackupManager = BackupManager(context)
	private val prefs: SharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILENAME, 0)

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

	fun getActiveAccountName(): String {
		return prefs.getString(ACTIVE_ACCOUNT_NAME, "").orEmpty()
	}

	fun getActivePublicKey(): String {
		val activeAccountName = getActiveAccountName()
		return prefs.getString(activeAccountName, "").orEmpty()
	}

	fun setActiveAccount(publicKey: String, accountName: String) {
		prefs.edit {
			putString(ACTIVE_ACCOUNT_NAME, accountName)
			putString(accountName, publicKey)
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