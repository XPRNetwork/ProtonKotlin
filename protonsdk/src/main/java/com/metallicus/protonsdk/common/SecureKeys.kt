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
import com.metallicus.protonsdk.eosio.commander.ec.EosPrivateKey
import com.metallicus.protonsdk.securestorage.SecurePreferences
import com.metallicus.protonsdk.securestorage.SecureStorageException
import timber.log.Timber

class SecureKeys(private val context: Context) {
	companion object {
		const val SHARED_PREFS_FILENAME = "protonsdk.secure_keys"
	}

	//private val backupManager: BackupManager = BackupManager(context)

	fun hasKeys(): Boolean {
		SecurePreferences.setSharedPreferencesName(SHARED_PREFS_FILENAME)
		val securePrefs = SecurePreferences.getSharedPreferences(context)
		return securePrefs.all.isNotEmpty()
	}

	fun checkPin(pin: String): Boolean {
		var isValid = false
		SecurePreferences.setSharedPreferencesName(SHARED_PREFS_FILENAME)
		val securePrefs = SecurePreferences.getSharedPreferences(context)
		if (securePrefs.all.isNotEmpty()) {
			isValid = try {
				val firstAccount = securePrefs.all.entries.iterator().next()
				val publicKey = firstAccount.key
				val privateKey = SecurePreferences.getStringValue(context, publicKey, pin, "")
				EosPrivateKey(privateKey).publicKey.toString() == publicKey
			} catch (e: Exception) {
				false
			}
		}
		return isValid
	}

	fun resetKeys(oldPin: String, newPin: String): Boolean {
		return try {
			SecurePreferences.setSharedPreferencesName(SHARED_PREFS_FILENAME)
			val securePrefs = SecurePreferences.getSharedPreferences(context)
			securePrefs.all.forEach { secureKey ->
				val publicKey = secureKey.key
				val privateKey = SecurePreferences.getStringValue(context, publicKey, oldPin, "")
				privateKey?.let {
					SecurePreferences.setValue(context, publicKey, it, newPin)
				}
			}
			true
		} catch (e: SecureStorageException) {
			Timber.d(e)
			false
		}
	}

	fun keyExists(publicKey: String): Boolean {
		SecurePreferences.setSharedPreferencesName(SHARED_PREFS_FILENAME)
		return SecurePreferences.contains(context, publicKey)
	}

	fun getPrivateKey(publicKey: String, pin: String): String? {
		SecurePreferences.setSharedPreferencesName(SHARED_PREFS_FILENAME)
		return SecurePreferences.getStringValue(context, publicKey, pin, "")
	}

	fun addKey(publicKey: String, privateKey: String, pin: String): Boolean {
		return try {
			SecurePreferences.setSharedPreferencesName(SHARED_PREFS_FILENAME)
			SecurePreferences.setValue(context, publicKey, privateKey, pin)
			//backupManager.dataChanged()
			true
		} catch (e: SecureStorageException) {
			Timber.d(e)
			false
		}
	}

	fun removeKey(publicKey: String) {
		try {
			SecurePreferences.setSharedPreferencesName(SHARED_PREFS_FILENAME)
			SecurePreferences.removeValue(context, publicKey)
			//backupManager.dataChanged()
		} catch (e: SecureStorageException) {
			Timber.d(e)
		}
	}

	fun removeAll() {
		try {
			SecurePreferences.setSharedPreferencesName(SHARED_PREFS_FILENAME)
			SecurePreferences.clearAllValues(context)
			//backupManager.dataChanged()
		} catch (e: SecureStorageException) {
			Timber.d(e)
		}
	}
}