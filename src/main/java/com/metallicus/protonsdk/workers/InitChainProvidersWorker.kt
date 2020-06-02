package com.metallicus.protonsdk.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.metallicus.protonsdk.model.ChainProvider
import com.metallicus.protonsdk.repository.ChainProviderRepository
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import timber.log.Timber

class InitChainProvidersWorker
@AssistedInject constructor(
	@Assisted context: Context,
	@Assisted params: WorkerParameters,
	private val chainProviderRepository: ChainProviderRepository
) : CoroutineWorker(context, params) {
	override suspend fun doWork(): Result {
		val chainProvidersUrl = inputData.getString("chainProvidersUrl").orEmpty()

		return try {
			val response = chainProviderRepository.fetchChainProviders(chainProvidersUrl)
			if (response.isSuccessful) {
				chainProviderRepository.removeAll()

				val gson = Gson()
				response.body()?.entrySet()?.forEach { entry ->
					val chainProvider = gson.fromJson(entry.value, ChainProvider::class.java)

					chainProviderRepository.addChainProvider(chainProvider)
				}

				Result.success()
			} else {
				val msg = response.errorBody()?.string()
				val errorMsg = if (msg.isNullOrEmpty()) {
					response.message()
				} else {
					msg
				}
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