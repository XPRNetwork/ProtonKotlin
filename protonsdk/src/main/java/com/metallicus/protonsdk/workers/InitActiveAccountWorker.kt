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
import com.google.gson.JsonSyntaxException
import com.metallicus.protonsdk.R
import com.metallicus.protonsdk.common.Prefs
import com.metallicus.protonsdk.common.ProtonError
import com.metallicus.protonsdk.model.AccountContact
import com.metallicus.protonsdk.model.AccountRefundsXPRInfo
import com.metallicus.protonsdk.model.AccountVotersXPRInfo
import com.metallicus.protonsdk.repository.AccountContactRepository
import com.metallicus.protonsdk.repository.AccountRepository
import com.metallicus.protonsdk.repository.ChainProviderRepository
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import timber.log.Timber
import java.lang.Exception

class InitActiveAccountWorker
@AssistedInject constructor(
	@Assisted context: Context,
	@Assisted params: WorkerParameters,
	private val prefs: Prefs,
	private val chainProviderRepository: ChainProviderRepository,
	private val accountRepository: AccountRepository,
	private val accountContactRepository: AccountContactRepository
) : CoroutineWorker(context, params) {

	private val usersInfoTableScope = context.getString(R.string.usersInfoTableScope)
	private val usersInfoTableCode = context.getString(R.string.usersInfoTableCode)
	private val usersInfoTableName = context.getString(R.string.usersInfoTableName)

	private val votersXPRInfoTableScope = context.getString(R.string.votersXPRInfoTableScope)
	private val votersXPRInfoTableCode = context.getString(R.string.votersXPRInfoTableCode)
	private val votersXPRInfoTableName = context.getString(R.string.votersXPRInfoTableName)

	private val refundsXPRInfoTableCode = context.getString(R.string.refundsXPRInfoTableCode)
	private val refundsXPRInfoTableName = context.getString(R.string.refundsXPRInfoTableName)

	@Suppress("BlockingMethodInNonBlockingContext")
	override suspend fun doWork(): Result {
		return try {
			val chainId = prefs.activeChainId
			val accountName = prefs.getActiveAccountName()

			val chainProvider = chainProviderRepository.getChainProvider(chainId)

			val response = accountRepository.fetchAccount(chainProvider.chainUrl, accountName)
			if (response.isSuccessful) {
				response.body()?.let { account ->
					account.accountChainId = chainId

					val accountContact = fetchAccountContact(chainProvider.chainUrl, accountName)
					account.accountContact = accountContact

					val accountVotersXPRInfo = fetchAccountVotersXPRInfo(chainProvider.chainUrl, accountName)
					account.votersXPRInfo = accountVotersXPRInfo

					val accountRefundsXPRInfo = fetchAccountRefundsXPRInfo(chainProvider.chainUrl, accountName)
					account.refundsXPRInfo = accountRefundsXPRInfo

					accountRepository.addAccount(account)

					Result.success()
				} ?: Result.failure()
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
					.putInt(ProtonError.ERROR_CODE_KEY, ProtonError.ACCOUNT_NOT_FOUND)
					.build()

				Result.failure(errorData)
			}
		} catch (e: Exception) {
			Timber.d(e)

			Result.failure()
		}
	}

	private suspend fun fetchAccountContact(chainUrl: String, accountName: String): AccountContact {
		val accountContact = AccountContact(accountName)
		accountContact.accountName = accountName

		val response = accountContactRepository.fetchAccountContact(
			chainUrl, accountName, usersInfoTableScope, usersInfoTableCode, usersInfoTableName)
		if (response.isSuccessful) {
			val userInfoRows = response.body()

			val rows = userInfoRows?.getAsJsonArray("rows")
			val size = rows?.size() ?: 0
			if (size > 0) {
				val userInfo = rows?.get(0)?.asJsonObject
				accountContact.name = userInfo?.get("name")?.asString.orEmpty()
				accountContact.avatar = userInfo?.get("avatar")?.asString.orEmpty()
				val verifiedInt = userInfo?.get("verified")?.asInt ?: 0
				accountContact.verified = verifiedInt == 1
			}
		} else {
			val msg = response.errorBody()?.string()
			val errorMsg = if (msg.isNullOrEmpty()) {
				response.message()
			} else {
				msg
			}

			Timber.d(errorMsg)
		}

		return accountContact
	}

	private suspend fun fetchAccountVotersXPRInfo(chainUrl: String, accountName: String): AccountVotersXPRInfo {
		var accountVotersXPRInfo = AccountVotersXPRInfo()

		val response = accountContactRepository.fetchAccountVotersXPRInfo(
			chainUrl, accountName, votersXPRInfoTableScope, votersXPRInfoTableCode, votersXPRInfoTableName)
		if (response.isSuccessful) {
			val votersXPRInfoRows = response.body()

			val rows = votersXPRInfoRows?.getAsJsonArray("rows")
			val size = rows?.size() ?: 0
			if (size > 0) {
				val votersXPRInfo = rows?.get(0)?.asJsonObject

				try {
					accountVotersXPRInfo = Gson().fromJson(votersXPRInfo, AccountVotersXPRInfo::class.java)
				} catch(e: JsonSyntaxException) {
					Timber.e(e)
				}
			}
		} else {
			val msg = response.errorBody()?.string()
			val errorMsg = if (msg.isNullOrEmpty()) {
				response.message()
			} else {
				msg
			}

			Timber.e(errorMsg)
		}

		return accountVotersXPRInfo
	}

	private suspend fun fetchAccountRefundsXPRInfo(chainUrl: String, accountName: String): AccountRefundsXPRInfo {
		var accountRefundsXPRInfo = AccountRefundsXPRInfo()

		val refundsXPRInfoTableScope = accountName

		val response = accountContactRepository.fetchAccountRefundsXPRInfo(
			chainUrl, accountName, refundsXPRInfoTableScope, refundsXPRInfoTableCode, refundsXPRInfoTableName)
		if (response.isSuccessful) {
			val refundsXPRInfoRows = response.body()

			val rows = refundsXPRInfoRows?.getAsJsonArray("rows")
			val size = rows?.size() ?: 0
			if (size > 0) {
				val refundsXPRInfo = rows?.get(0)?.asJsonObject

				try {
					accountRefundsXPRInfo = Gson().fromJson(refundsXPRInfo, AccountRefundsXPRInfo::class.java)
				} catch(e: JsonSyntaxException) {
					Timber.e(e)
				}
			}
		} else {
			val msg = response.errorBody()?.string()
			val errorMsg = if (msg.isNullOrEmpty()) {
				response.message()
			} else {
				msg
			}

			Timber.d(errorMsg)
		}

		return accountRefundsXPRInfo
	}

	@AssistedInject.Factory
	interface Factory : ChildWorkerFactory
}