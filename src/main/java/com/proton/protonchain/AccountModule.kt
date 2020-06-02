package com.proton.protonchain

import android.content.Context
import com.proton.protonchain.common.AccountPrefs
import com.proton.protonchain.di.DaggerInjector
import com.proton.protonchain.model.*
import com.proton.protonchain.repository.AccountRepository
import timber.log.Timber
import javax.inject.Inject

class AccountModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var accountRepository: AccountRepository

	@Inject
	lateinit var accountPrefs: AccountPrefs

	init {
		DaggerInjector.component.inject(this)
	}

	suspend fun getAccountNamesForKey(chainUrl: String, publicKey: String): List<String> {
		val accountNames = mutableListOf<String>()

		try {
			val keyAccountResponse = accountRepository.fetchStateHistoryKeyAccount(chainUrl, publicKey)
			if (keyAccountResponse.isSuccessful) {
				keyAccountResponse.body()?.let {
					accountNames.addAll(it.accountNames)
				}
			}
		} catch (e: Exception) {
			Timber.d(e)
		}

		return accountNames
	}

	suspend fun addAccount(selectableAccount: SelectableAccount, pin: String): Resource<ChainAccount> {
		val publicKey = selectableAccount.privateKey.publicKey.toString()
		val privateKey = selectableAccount.privateKey.toString()
		accountPrefs.addAccountKey(publicKey, privateKey, pin)

		val chainId = selectableAccount.chainProvider.chainId
		val chainUrl = selectableAccount.chainProvider.chainUrl
		val usersInfoTableScope = selectableAccount.chainProvider.usersInfoTableScope
		val usersInfoTableCode = selectableAccount.chainProvider.usersInfoTableCode
		val accountName = selectableAccount.accountName

		return try {
			val response = accountRepository.fetchAccount(chainUrl, accountName)
			if (response.isSuccessful) {
				response.body()?.let { account ->
					account.accountChainId = chainId

					val accountContact = AccountContact(accountName)
					accountContact.accountName = accountName
					accountContact.chainId = chainId

					val accountInfoResponse = accountRepository.fetchAccountInfo(chainUrl, accountName, usersInfoTableScope, usersInfoTableCode)
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
					}

					account.accountContact = accountContact

					accountRepository.addAccount(account)

					val chainAccount = accountRepository.getChainAccount(chainId, accountName)

					Resource.success(chainAccount)
				} ?: Resource.error("Account Error", null)
			} else {
				val msg = response.errorBody()?.string()
				val errorMsg = if (msg.isNullOrEmpty()) {
					response.message()
				} else {
					msg
				}

				Resource.error(errorMsg, null)
			}
		} catch (e: Exception) {
			Resource.error("Connection Error or Timeout: Please check your network settings", null)
		}
	}
}