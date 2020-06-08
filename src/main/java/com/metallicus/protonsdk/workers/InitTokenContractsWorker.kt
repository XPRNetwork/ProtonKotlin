package com.metallicus.protonsdk.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.metallicus.protonsdk.R
import com.metallicus.protonsdk.model.TokenContract
import com.metallicus.protonsdk.repository.ChainProviderRepository
import com.metallicus.protonsdk.repository.TokenContractRepository
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

	private val protonChainTokensTableScope = context.getString(R.string.protonChainTokensTableScope)
	private val protonChainTokensTableCode = context.getString(R.string.protonChainTokensTableCode)
	private val protonChainTokensTableName = context.getString(R.string.protonChainTokensTableName)

	override suspend fun doWork(): Result {
		return try {
			val chainProviders = chainProviderRepository.getAllChainProviders()
			if (chainProviders.isNotEmpty()) {
				var tokenContractsResult = Result.success()

				tokenContractRepository.removeAll()

				chainProviders.forEach { chainProvider ->
					val tokenContractsResponse = tokenContractRepository.fetchTokenContracts(
						chainProvider.chainUrl,
						protonChainTokensTableScope,
						protonChainTokensTableCode,
						protonChainTokensTableName)
					if (tokenContractsResponse.isSuccessful) {
						val responseJsonObject = tokenContractsResponse.body()

						val gson = Gson()
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