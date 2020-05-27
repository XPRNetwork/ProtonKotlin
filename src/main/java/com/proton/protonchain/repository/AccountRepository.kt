package com.proton.protonchain.repository

import com.proton.protonchain.api.ProtonChainService
import com.proton.protonchain.db.AccountDao
import com.proton.protonchain.model.Account
import com.proton.protonchain.model.KeyAccount
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
	private val accountDao: AccountDao,
	private val protonChainService: ProtonChainService
) {
	fun addAccount(account: Account) {
		accountDao.insert(account)
	}

	fun updateAccount(account: Account) {
		accountDao.update(account)
	}

	suspend fun fetchStateHistoryKeyAccount(chainUrl: String, publicKey: String): Response<KeyAccount> {
		return protonChainService.getStateHistoryKeyAccountsAsync("$chainUrl/v2/state/get_key_accounts", publicKey)
	}
}
