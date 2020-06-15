package com.metallicus.protonsdk.repository

import com.google.gson.JsonObject
import com.metallicus.protonsdk.api.AccountBody
import com.metallicus.protonsdk.api.ProtonChainService
import com.metallicus.protonsdk.api.TableRowsBody
import com.metallicus.protonsdk.db.AccountDao
import com.metallicus.protonsdk.db.CurrencyBalanceDao
import com.metallicus.protonsdk.model.Account
import com.metallicus.protonsdk.model.ChainAccount
import com.metallicus.protonsdk.model.KeyAccount
import com.metallicus.protonsdk.model.TokenCurrencyBalance
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
	private val accountDao: AccountDao,
	private val currencyBalanceDao: CurrencyBalanceDao,
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

	suspend fun fetchAccount(chainUrl: String, accountName: String): Response<Account> {
		return protonChainService.getAccountAsync("$chainUrl/v1/chain/get_account", AccountBody(accountName))
	}

	suspend fun fetchAccountInfo(chainUrl: String, accountName: String, usersInfoTableScope: String, usersInfoTableCode: String, usersInfoTableName: String): Response<JsonObject> {
		return protonChainService.getTableRows("$chainUrl/v1/chain/get_table_rows", TableRowsBody(usersInfoTableScope, usersInfoTableCode, usersInfoTableName, accountName, accountName))
	}

	suspend fun fetchKeyAccount(stateHistoryUrl: String, publicKey: String): Response<KeyAccount> {
		return protonChainService.getKeyAccounts("$stateHistoryUrl/v2/state/get_key_accounts", publicKey)
	}

	suspend fun fetchCurrencyBalances(chainUrl: String, accountName: String): Response<JsonObject> {
		return protonChainService.getCurrencyBalances("$chainUrl/v2/state/get_tokens", accountName)
	}

	suspend fun getTokenCurrencyBalances(accountName: String, tokens: List<String>): List<TokenCurrencyBalance> {
		return if (tokens.isNullOrEmpty())
			currencyBalanceDao.findByAccountName(accountName)
		else {
			val contracts = mutableListOf<String>()
			val symbols = mutableListOf<String>()
			tokens.forEach {
				val token = it.split(":")
				contracts.add(token[0])
				symbols.add(token[1])
			}
			currencyBalanceDao.findByAccountTokenContract( accountName, contracts, symbols)
		}
	}
}
