package com.proton.protonchain

import android.content.Context
import com.proton.protonchain.di.DaggerInjector
import com.proton.protonchain.repository.AccountRepository
import timber.log.Timber
import javax.inject.Inject

class AccountModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var accountRepository: AccountRepository

	init {
		DaggerInjector.component.inject(this)
	}

	suspend fun getAccountNamesForKey(chainUrl: String, publicKey: String): List<String> {
		val accountNames = mutableListOf<String>()

		try {
			val keyAccountResponse = accountRepository.fetchStateHistoryKeyAccount(chainUrl, publicKey)
			if (keyAccountResponse.isSuccessful) {
				keyAccountResponse.body()?.let {
					accountNames.addAll(it.accountNames)
				}
			}
		} catch (e: Exception) {
			Timber.d(e)
		}

		return accountNames
	}
}