package com.proton.protonchain.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.proton.protonchain.model.TokenContract
import com.proton.protonchain.repository.ChainProviderRepository
import com.proton.protonchain.repository.TokenContractRepository
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import timber.log.Timber
import java.lang.Exception

class InitTokenContractsWorker
@AssistedInject constructor(
	@Assisted context: Context,
	@Assisted params: WorkerParameters,
	private val chainProviderRepository: ChainProviderRepository,
	private val tokenContractRepository: TokenContractRepository
) : CoroutineWorker(context, params) {
	override suspend fun doWork(): Result {
		return try {
			val chainProviders = chainProviderRepository.getAllChainProviders()
			if (chainProviders.isNotEmpty()) {
				var tokenContractsResult = Result.success()

				tokenContractRepository.removeAll()

				chainProviders.forEach { chainProvider ->
					val tokenContractsResponse = tokenContractRepository.fetchTokenContracts(chainProvider.chainUrl, chainProvider.tokensTableScope, chainProvider.tokensTableCode)
					if (tokenContractsResponse.isSuccessful) {
						val responseJsonObject = tokenContractsResponse.body()

						//val gsonBuilder = GsonBuilder().registerTypeAdapter(Int::class.java, IntTypeAdapter())
						val gson = Gson()//gsonBuilder.create()
						val rows = responseJsonObject?.getAsJsonArray("rows")
						rows?.forEach {
							val tokenContractJsonObject = it.asJsonObject

							val tokenContract = gson.fromJson(tokenContractJsonObject, TokenContract::class.java)
							tokenContract.chainId = chainProvider.chainId

							// TODO: add params from get_currency_stats

							tokenContractRepository.addTokenContract(tokenContract)
						}
					} else {
						tokenContractsResult = Result.failure()
					}
				}

				tokenContractsResult
			} else {
				Result.failure()
			}
		} catch (e: Exception) {
			Timber.d(e)

			Result.failure()
		}
	}

	@AssistedInject.Factory
	interface Factory : ChildWorkerFactory
}