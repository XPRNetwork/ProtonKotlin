package com.metallicus.protonsdk.common

import android.content.Context
import com.metallicus.protonsdk.eosio.commander.ec.EosPrivateKey
import com.metallicus.protonsdk.securestorage.SecurePreferences
import com.metallicus.protonsdk.securestorage.SecureStorageException
import timber.log.Timber
import java.lang.Exception

class AccountPrefs(private val context: Context) {
	companion object {
		const val SHARED_PREFS_FILENAME = "protonsdk.accounts"
	}

	//private val backupManager: BackupManager = BackupManager(context)

	fun checkPin(pin: String): Boolean {
		var isValid = false
		SecurePreferences.setSharedPreferencesName(SHARED_PREFS_FILENAME)
		val securePrefs = SecurePreferences.getSharedPreferences(context)
		if (securePrefs.all.isNotEmpty()) {
			isValid = try {
				val firstAccount = securePrefs.all.entries.iterator().next()
				val publicKey = firstAccount.key
				val privateKey = getAccountKey(firstAccount.key, pin)
				EosPrivateKey(privateKey).publicKey.toString() == publicKey
			} catch (e: Exception) {
				false
			}
		}
		return isValid
	}

	fun hasAccounts(): Boolean {
		SecurePreferences.setSharedPreferencesName(SHARED_PREFS_FILENAME)
		val securePrefs = SecurePreferences.getSharedPreferences(context)
		return securePrefs.all.isNotEmpty()
	}

	fun accountExists(publicKey: String): Boolean {
		SecurePreferences.setSharedPreferencesName(SHARED_PREFS_FILENAME)
		return SecurePreferences.contains(context, publicKey)
	}

	fun getAccountKey(publicKey: String, pin: String): String? {
		SecurePreferences.setSharedPreferencesName(SHARED_PREFS_FILENAME)
		return SecurePreferences.getStringValue(context, publicKey, pin, "")
	}

	fun getAccountKeys(): ArrayList<String> {
		val publicKeys = arrayListOf<String>()
		SecurePreferences.setSharedPreferencesName(SHARED_PREFS_FILENAME)
		val securePrefs = SecurePreferences.getSharedPreferences(context)
		val allAccounts = securePrefs.all
		for (entry in allAccounts.entries) {
			publicKeys.add(entry.key)
		}
		return publicKeys
	}

	fun addAccountKey(publicKey: String, privateKey: String, pin: String): Boolean {
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

	fun remove(publicKey: String) {
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