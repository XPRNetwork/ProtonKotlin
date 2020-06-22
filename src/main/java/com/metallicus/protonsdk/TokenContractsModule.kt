package com.metallicus.protonsdk

import android.content.Context
import com.metallicus.protonsdk.common.Prefs
import com.metallicus.protonsdk.di.DaggerInjector
import com.metallicus.protonsdk.model.TokenContract
import com.metallicus.protonsdk.repository.TokenContractRepository
import timber.log.Timber
import javax.inject.Inject

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

	suspend fun updateExchangeRates(exchangeRatesUrl: String, tokenContractsMap: Map<String, String>) {
		val exchangeRatesResponse = tokenContractRepository.fetchExchangeRates(exchangeRatesUrl)
		if (exchangeRatesResponse.isSuccessful) {
			val exchangeRatesJsonArray = exchangeRatesResponse.body()
			exchangeRatesJsonArray?.forEach {
				val exchangeRate = it.asJsonObject
				val contract = exchangeRate.get("contract").asString
				val symbol = exchangeRate.get("symbol").asString
				val rates = exchangeRate.get("rates").asJsonObject

				try {
					val tokenContractId = tokenContractsMap.getValue("$contract:$symbol")
					tokenContractRepository.updateRates(tokenContractId, rates.toString())
				} catch (e: Exception) {
					Timber.d(e.localizedMessage)
				}
			}
		}
	}
}