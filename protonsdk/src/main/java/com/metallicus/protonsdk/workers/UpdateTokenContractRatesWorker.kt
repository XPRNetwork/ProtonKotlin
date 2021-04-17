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
package com.metallicus.protonsdk.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.metallicus.protonsdk.common.Prefs
import com.metallicus.protonsdk.model.TokenContractRate
import com.metallicus.protonsdk.repository.ChainProviderRepository
import com.metallicus.protonsdk.repository.TokenContractRepository
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import timber.log.Timber
import java.lang.Exception

class UpdateTokenContractRatesWorker
@AssistedInject constructor(
	@Assisted context: Context,
	@Assisted params: WorkerParameters,
	private val prefs: Prefs,
	private val chainProviderRepository: ChainProviderRepository,
	private val tokenContractRepository: TokenContractRepository
) : CoroutineWorker(context, params) {

	override suspend fun doWork(): Result {
		return try {
			val chainProvider = chainProviderRepository.getChainProvider(prefs.activeChainId)

			val tokenContracts = tokenContractRepository.getTokenContracts()

			val tokenContractsMap = mutableMapOf<String, String>()
			tokenContracts.forEach {
				tokenContractsMap["${it.contract}:${it.getSymbol()}"] = it.id
			}

			val exchangeRateUrl = chainProvider.protonChainUrl + chainProvider.exchangeRatePath

			val exchangeRatesResponse = tokenContractRepository.fetchExchangeRates(exchangeRateUrl)
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

					try {
						val tokenContractId = tokenContractsMap.getValue("$contract:$symbol")
						tokenContractRepository.updateRates(tokenContractId, ratesMap, rank)
					} catch (e: Exception) {
						Timber.d(e.localizedMessage)
					}
				}
			}

			Result.success()
		} catch (e: Exception) {
			Timber.d(e)

			Result.failure()
		}
	}

	@AssistedInject.Factory
	interface Factory : ChildWorkerFactory
}