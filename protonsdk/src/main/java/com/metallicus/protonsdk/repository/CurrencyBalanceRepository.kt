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
package com.metallicus.protonsdk.repository

import com.google.gson.JsonObject
import com.metallicus.protonsdk.api.ProtonChainService
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
