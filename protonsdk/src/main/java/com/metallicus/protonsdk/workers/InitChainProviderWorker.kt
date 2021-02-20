/*
 * Copyright (c) 2020 Proton Chain LLC, Delaware
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
		const val PROTON_CHAIN_URL = "protonChainUrl"
	}

	@Suppress("BlockingMethodInNonBlockingContext")
	override suspend fun doWork(): Result {
		val chainUrl = inputData.getString(PROTON_CHAIN_URL).orEmpty()

		return try {
			val response = chainProviderRepository.fetchChainProvider(chainUrl)
			if (response.isSuccessful) {
				chainProviderRepository.removeAll()

				val chainProvider = Gson().fromJson(response.body(), ChainProvider::class.java)
				chainProvider.chainApiUrl = chainUrl

//				val timeout = 4000L // 4 milliseconds
//				val acceptableChainBlockDiff = 350L
//				val acceptableHyperionHistoryBlockDiff = 30L
//
//				var fastestChainUrl = chainProvider.chainUrl
//				var fastestChainUrlTime = timeout
//
//				chainProvider.chainUrls.forEach {
//					val chainUrlResponse = chainProviderRepository.getChainInfo(it)
//					if (chainUrlResponse.isSuccessful) {
//						chainUrlResponse.body()?.let { chainInfo ->
//							val blockDiff = chainInfo.headBlockNum - chainInfo.lastIrreversibleBlockNum
//							val responseTime = chainUrlResponse.raw().receivedResponseAtMillis - chainUrlResponse.raw().sentRequestAtMillis
//							if (responseTime < fastestChainUrlTime && blockDiff < acceptableChainBlockDiff) {
//								fastestChainUrl = it
//								fastestChainUrlTime = responseTime
//							}
//						}
//					}
//				}
//
//				chainProvider.chainUrl = fastestChainUrl
//
//				var fastestHyperionHistoryUrl = chainProvider.hyperionHistoryUrl
//				var fastestHyperionHistoryUrlTime = timeout
//
//				chainProvider.hyperionHistoryUrls.forEach {
//					val healthResponse = chainProviderRepository.getHealth(it)
//					if (healthResponse.isSuccessful) {
//						var blockDiff = acceptableHyperionHistoryBlockDiff
//
//						healthResponse.body()?.let { body ->
//							var headBlockNum = 0L
//							var lastIndexedBlock = 0L
//							val health = body.get("health").asJsonArray
//							health.forEach { healthElement ->
//								val serviceObj = healthElement.asJsonObject
//								if (serviceObj.get("service").asString == "NodeosRPC") {
//									val serviceDataObj = serviceObj.get("service_data").asJsonObject
//									headBlockNum = serviceDataObj.get("head_block_num").asLong
//								}
//								if (serviceObj.get("service").asString == "Elasticsearch") {
//									val serviceDataObj = serviceObj.get("service_data").asJsonObject
//									lastIndexedBlock = serviceDataObj.get("last_indexed_block").asLong
//								}
//							}
//
//							if (headBlockNum != 0L && lastIndexedBlock != 0L) {
//								blockDiff = headBlockNum - lastIndexedBlock
//							}
//						}
//
//						val responseTime = healthResponse.raw().receivedResponseAtMillis - healthResponse.raw().sentRequestAtMillis
//						if (responseTime < fastestHyperionHistoryUrlTime && blockDiff < acceptableHyperionHistoryBlockDiff) {
//							fastestHyperionHistoryUrl = it
//							fastestHyperionHistoryUrlTime = responseTime
//						}
//					}
//				}
//
//				chainProvider.hyperionHistoryUrl = fastestHyperionHistoryUrl

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