/*
 * Copyright (c) 2021 Proton Chain LLC, Delaware
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

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.metallicus.protonsdk.api.ProtonChainService
import com.metallicus.protonsdk.api.TableRowsBody
import com.metallicus.protonsdk.db.TokenContractDao
import com.metallicus.protonsdk.model.TokenContract
import com.metallicus.protonsdk.model.TokenContractRate
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenContractRepository @Inject constructor(
	private val tokenContractDao: TokenContractDao,
	private val protonChainService: ProtonChainService
) {
	suspend fun removeAll() {
		tokenContractDao.removeAll()
	}

	suspend fun addTokenContract(tokenContract: TokenContract) {
		tokenContractDao.insert(tokenContract)
	}

	suspend fun fetchTokenContracts(chainUrl: String, tokensTableScope: String, tokensTableCode: String, tokensTableName: String): Response<JsonObject> {
		return protonChainService.getTableRows("$chainUrl/v1/chain/get_table_rows", TableRowsBody(tokensTableScope, tokensTableCode, tokensTableName, "", "", 100))
	}

	suspend fun getTokenContract(tokenContractId: String): TokenContract {
		return tokenContractDao.findById(tokenContractId)
	}

	suspend fun getTokenContracts(): List<TokenContract> {
		return tokenContractDao.findAll()
	}

	suspend fun fetchExchangeRates(exchangeRateUrl: String): Response<JsonArray> {
		return protonChainService.getExchangeRates("$exchangeRateUrl/info")
	}

	suspend fun updateRates(tokenContractId: String, rates: Map<String, TokenContractRate>, rank: Int) {
		return tokenContractDao.updateRates(tokenContractId, rates, rank)
	}
}
