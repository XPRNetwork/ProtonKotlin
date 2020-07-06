package com.metallicus.protonsdk.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.metallicus.protonsdk.common.Prefs
import com.metallicus.protonsdk.common.ProtonError
import com.metallicus.protonsdk.model.ChainProvider
import com.metallicus.protonsdk.repository.ChainProviderRepository
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import timber.log.Timber

class InitChainProviderWorker
@AssistedInject constructor(
	@Assisted context: Context,
	@Assisted params: WorkerParameters,
	private val prefs: Prefs,
	private val chainProviderRepository: ChainProviderRepository
) : CoroutineWorker(context, params) {
	companion object {
		const val CHAIN_PROVIDER_URL = "chainProviderUrl"
	}

	override suspend fun doWork(): Result {
		val chainProviderUrl = inputData.getString(CHAIN_PROVIDER_URL).orEmpty()

		return try {
			val response = chainProviderRepository.fetchChainProvider(chainProviderUrl)
			if (response.isSuccessful) {
				chainProviderRepository.removeAll()

				val chainProvider = Gson().fromJson(response.body(), ChainProvider::class.java)

				chainProviderRepository.addChainProvider(chainProvider)

				prefs.activeChainId = chainProvider.chainId

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