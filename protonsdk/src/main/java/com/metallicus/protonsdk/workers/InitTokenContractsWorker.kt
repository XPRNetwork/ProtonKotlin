package com.metallicus.protonsdk.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.metallicus.protonsdk.R
import com.metallicus.protonsdk.common.Prefs
import com.metallicus.protonsdk.common.ProtonError
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

	@Suppress("BlockingMethodInNonBlockingContext")
	override suspend fun doWork(): Result {
		return try {
			val chainProvider = chainProviderRepository.getChainProvider(prefs.activeChainId)

			tokenContractRepository.removeAll()

			val response = tokenContractRepository.fetchTokenContracts(
				chainProvider.chainUrl,
				protonChainTokensTableScope,
				protonChainTokensTableCode,
				protonChainTokensTableName)
			if (response.isSuccessful) {
				val responseJsonObject = response.body()

				val gson = Gson()
				val rows = responseJsonObject?.getAsJsonArray("rows")
				rows?.forEach {
					val tokenContractJsonObject = it.asJsonObject

					val tokenContract = gson.fromJson(tokenContractJsonObject, TokenContract::class.java)
					tokenContract.rates = mapOf(Pair("USD", 0.0))

					// TODO: add supply, maxSupply, and issuer from get_currency_stats

					tokenContractRepository.addTokenContract(tokenContract)
				}

				Result.success()
			} else {
				val msg = response.errorBody()?.string()
				val errorMsg = if (msg.isNullOrEmpty()) {
					response.message()
				} else {
					msg
				}
				Timber.d(errorMsg)

				val errorData = Data.Builder()
					.putString(ProtonError.ERROR_MESSAGE_KEY, errorMsg)
					.putInt(ProtonError.ERROR_CODE_KEY, response.code())
					.build()

				Result.failure(errorData)
			}
		} catch (e: Exception) {
			Timber.d(e)

			Result.failure()
		}
	}

	@AssistedInject.Factory
	interface Factory : ChildWorkerFactory
}