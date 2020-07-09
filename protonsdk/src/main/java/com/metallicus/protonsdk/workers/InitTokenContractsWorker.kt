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