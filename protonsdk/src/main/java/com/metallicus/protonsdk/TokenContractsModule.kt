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
package com.metallicus.protonsdk

import android.content.Context
import com.google.gson.Gson
import com.metallicus.protonsdk.common.Prefs
import com.metallicus.protonsdk.di.DaggerInjector
import com.metallicus.protonsdk.model.ChainProvider
import com.metallicus.protonsdk.model.TokenContract
import com.metallicus.protonsdk.model.TokenContractRate
import com.metallicus.protonsdk.repository.TokenContractRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Helper class used for [TokenContract] based operations
 */
class TokenContractsModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var tokenContractRepository: TokenContractRepository

	@Inject
	lateinit var prefs: Prefs

	init {
		DaggerInjector.component.inject(this)
	}

	suspend fun getTokenContract(tokenContractId: String): TokenContract {
		return tokenContractRepository.getTokenContract(tokenContractId)
	}

	suspend fun getTokenContracts(): List<TokenContract> {
		return tokenContractRepository.getTokenContracts()
	}

	suspend fun updateExchangeRates(exchangeRatesUrl: String, tokenContractsMap: Map<String, TokenContract>) {
		try {
			val exchangeRatesResponse = tokenContractRepository.fetchExchangeRates(exchangeRatesUrl)
			if (exchangeRatesResponse.isSuccessful) {
				val exchangeRatesJsonArray = exchangeRatesResponse.body()
				exchangeRatesJsonArray?.forEach {
					val exchangeRate = it.asJsonObject
					val contract = exchangeRate.get("contract").asString
					val symbol = exchangeRate.get("symbol").asString
					val rank = exchangeRate.get("rank").asInt

					val ratesMap = mutableMapOf<String, TokenContractRate>()

					val ratesJsonArray = exchangeRate.get("rates").asJsonArray
					ratesJsonArray.forEach { rateJsonElement ->
						val rateJsonObj = rateJsonElement.asJsonObject
						val currency = rateJsonObj.get("counterCurrency").asString
						val rate = Gson().fromJson(rateJsonObj, TokenContractRate::class.java)
						ratesMap[currency] = rate
					}

					val tokenContractId = "$contract:$symbol"
					if (tokenContractsMap.containsKey(tokenContractId)) {
						tokenContractsMap[tokenContractId]?.rates = ratesMap

						tokenContractRepository.updateRates(tokenContractId, ratesMap, rank)
					}
				}
			}
		} catch (e: Exception) {
			Timber.d(e.localizedMessage)
		}
	}
}