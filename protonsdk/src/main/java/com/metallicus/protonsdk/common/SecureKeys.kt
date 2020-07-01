package com.metallicus.protonsdk.common

import android.content.Context
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