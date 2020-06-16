package com.metallicus.protonsdk.repository

import com.google.gson.JsonObject
import com.metallicus.protonsdk.api.AccountBody
import com.metallicus.protonsdk.api.ProtonChainService
import com.metallicus.protonsdk.api.TableRowsBody
import com.metallicus.protonsdk.db.AccountDao
import com.metallicus.protonsdk.db.CurrencyBalanceDao
import com.metallicus.protonsdk.model.*
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyBalanceRepository @Inject constructor(
	private val currencyBalanceDao: CurrencyBalanceDao,
	private val protonChainService: ProtonChainService
) {
	suspend fun removeAll() {
		currencyBalanceDao.removeAll()
	}

	suspend fun addCurrencyBalance(currencyBalance: CurrencyBalance) {
		currencyBalanceDao.insert(currencyBalance)
	}

	suspend fun fetchCurrencyBalances(chainUrl: String, accountName: String): Response<JsonObject> {
		return protonChainService.getCurrencyBalances("$chainUrl/v2/state/get_tokens", accountName)
	}

	suspend fun getTokenCurrencyBalance(tokenContractId: String): TokenCurrencyBalance {
		return currencyBalanceDao.findByTokenContract(tokenContractId)
	}

	suspend fun getTokenCurrencyBalances(accountName: String): List<TokenCurrencyBalance> {
		return currencyBalanceDao.findByAccountName(accountName)
	}
}
