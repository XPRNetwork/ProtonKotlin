package com.metallicus.protonsdk.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.metallicus.protonsdk.R
import com.metallicus.protonsdk.common.Prefs
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
	private val prefs: Prefs,
	private val chainProviderRepository: ChainProviderRepository,
	private val tokenContractRepository: TokenContractRepository
) : CoroutineWorker(context, params) {

	private val protonChainTokensTableScope = context.getString(R.string.protonChainTokensTableScope)
	private val protonChainTokensTableCode = context.getString(R.string.protonChainTokensTableCode)
	private val protonChainTokensTableName = context.getString(R.string.protonChainTokensTableName)

	override suspend fun doWork(): Result {
		return try {
			val chainProvider = chainProviderRepository.getChainProvider(prefs.activeChainId)

			tokenContractRepository.removeAll()

			var tokenContractsResult = Result.success()

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
				val msg = tokenContractsResponse.errorBody()?.string()
				val errorMsg = if (msg.isNullOrEmpty()) {
					tokenContractsResponse.message()
				} else {
					msg
				}
				Timber.d(errorMsg)

				tokenContractsResult = Result.failure()
			}

			tokenContractsResult
		} catch (e: Exception) {
			Timber.d(e)

			Result.failure()
		}
	}

	@AssistedInject.Factory
	interface Factory : ChildWorkerFactory
}