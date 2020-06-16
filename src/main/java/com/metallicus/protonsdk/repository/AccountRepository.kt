package com.metallicus.protonsdk.repository

import com.metallicus.protonsdk.api.AccountBody
import com.metallicus.protonsdk.api.ProtonChainService
import com.metallicus.protonsdk.db.AccountDao
import com.metallicus.protonsdk.model.*
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
	private val accountDao: AccountDao,
	private val protonChainService: ProtonChainService
) {
	suspend fun removeAll() {
		accountDao.removeAll()
	}

	suspend fun addAccount(account: Account) {
		accountDao.insert(account)
	}

	suspend fun updateAccount(account: Account) {
		accountDao.update(account)
	}

	suspend fun getChainAccount(accountName: String): ChainAccount {
		return accountDao.findByAccountName(accountName)
	}

	suspend fun fetchKeyAccount(hyperionHistoryUrl: String, publicKey: String): Response<KeyAccount> {
		return protonChainService.getKeyAccounts("$hyperionHistoryUrl/v2/state/get_key_accounts", publicKey)
	}

	suspend fun fetchAccount(chainUrl: String, accountName: String): Response<Account> {
		return protonChainService.getAccount("$chainUrl/v1/chain/get_account", AccountBody(accountName))
	}
}
