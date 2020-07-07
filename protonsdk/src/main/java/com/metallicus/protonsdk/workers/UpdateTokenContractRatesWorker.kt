package com.metallicus.protonsdk.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.metallicus.protonsdk.common.Prefs
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

			val exchangeRateUrl = chainProvider.chainUrl + chainProvider.exchangeRatePath

			val exchangeRatesResponse = tokenContractRepository.fetchExchangeRates(exchangeRateUrl)
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

			Result.success()
		} catch (e: Exception) {
			Timber.d(e)

			Result.failure()
		}
	}

	@AssistedInject.Factory
	interface Factory : ChildWorkerFactory
}