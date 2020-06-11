package com.metallicus.protonsdk.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.metallicus.protonsdk.R
import com.metallicus.protonsdk.common.Prefs
import com.metallicus.protonsdk.model.AccountContact
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
	private val accountRepository: AccountRepository
) : CoroutineWorker(context, params) {

	private val usersInfoTableScope = context.getString(R.string.protonChainUsersInfoTableScope)
	private val usersInfoTableCode = context.getString(R.string.protonChainUsersInfoTableCode)
	private val usersInfoTableName = context.getString(R.string.protonChainUsersInfoTableName)

	override suspend fun doWork(): Result {
		return try {
			val chainId = prefs.activeChainId
			val accountName = prefs.activeAccountName

			val chainProvider = chainProviderRepository.getChainProvider(chainId)

			accountRepository.removeAll()

			val response = accountRepository.fetchAccount(chainProvider.chainUrl, accountName)
			if (response.isSuccessful) {
				response.body()?.let { account ->
					account.accountChainId = chainId

					val accountContact = AccountContact(accountName)
					accountContact.accountName = accountName
					accountContact.chainId = chainId

					val accountInfoResponse = accountRepository.fetchAccountInfo(
						chainProvider.chainUrl, accountName, usersInfoTableScope, usersInfoTableCode, usersInfoTableName)
					if (accountInfoResponse.isSuccessful) {
						val userInfoJsonObject = accountInfoResponse.body()

						val rows = userInfoJsonObject?.getAsJsonArray("rows")
						val size = rows?.size() ?: 0
						if (size > 0) {
							val userInfo = rows?.get(0)?.asJsonObject
							accountContact.name = userInfo?.get("name")?.asString.orEmpty()
							accountContact.avatar = userInfo?.get("avatar")?.asString.orEmpty()
						}
					} else {
						val msg = response.errorBody()?.string()
						val errorMsg = if (msg.isNullOrEmpty()) {
							response.message()
						} else {
							msg
						}

						Timber.d(errorMsg)

						// Don't fail if can't find usersinfo
						//Result.failure()
					}

					account.accountContact = accountContact

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