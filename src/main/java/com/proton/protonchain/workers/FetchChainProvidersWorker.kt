package com.proton.protonchain.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.proton.protonchain.model.ChainProvider
import com.proton.protonchain.repository.ChainProviderRepository
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import timber.log.Timber
import java.lang.Exception

class FetchChainProvidersWorker
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
				val gson = Gson()
				response.body()?.entrySet()?.forEach { entry ->
					val type = object : TypeToken<Map<String, Any>>() {}.type
					val chainProviderMap = gson.fromJson<Map<String, Any>>(entry.value, type)
					val chainProvider = ChainProvider(chainProviderMap)

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