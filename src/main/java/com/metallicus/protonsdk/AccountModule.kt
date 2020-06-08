package com.metallicus.protonsdk

import android.content.Context
import com.metallicus.protonsdk.common.SecureKeys
import com.metallicus.protonsdk.common.Prefs
import com.metallicus.protonsdk.common.Resource
import com.metallicus.protonsdk.di.DaggerInjector
import com.metallicus.protonsdk.model.*
import com.metallicus.protonsdk.repository.AccountRepository
import timber.log.Timber
import javax.inject.Inject

class AccountModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var accountRepository: AccountRepository

	@Inject
	lateinit var prefs: Prefs

	@Inject
	lateinit var secureKeys: SecureKeys

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

	suspend fun setSelectedAccount(selectableAccount: SelectableAccount, pin: String): Resource<ChainAccount> {
		val publicKey = selectableAccount.privateKey.publicKey.toString()
		val privateKey = selectableAccount.privateKey.toString()
		secureKeys.addKey(publicKey, privateKey, pin)

		val chainId = selectableAccount.chainProvider.chainId
		val chainUrl = selectableAccount.chainProvider.chainUrl
		val usersInfoTableScope = context.getString(R.string.protonChainUsersInfoTableScope)
		val usersInfoTableCode = context.getString(R.string.protonChainUsersInfoTableCode)
		val usersInfoTableName = context.getString(R.string.protonChainUsersInfoTableName)
		val accountName = selectableAccount.accountName

		return try {
			val response = accountRepository.fetchAccount(chainUrl, accountName)
			if (response.isSuccessful) {
				response.body()?.let { account ->
					account.accountChainId = chainId

					val accountContact = AccountContact(accountName)
					accountContact.accountName = accountName
					accountContact.chainId = chainId

					val accountInfoResponse = accountRepository.fetchAccountInfo(
						chainUrl, accountName, usersInfoTableScope, usersInfoTableCode, usersInfoTableName)
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

					prefs.selectedAccountChainId = chainId
					prefs.selectedAccountName = accountName

					return getSelectedAccount()
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

	suspend fun getSelectedAccount(): Resource<ChainAccount> {
		val chainId = prefs.selectedAccountChainId.orEmpty()
		val accountName = prefs.selectedAccountName.orEmpty()
		return if (chainId.isNotEmpty() && accountName.isNotEmpty()) {
			Resource.success(accountRepository.getChainAccount(chainId, accountName))
		} else {
			Resource.error("No Selected Account")
		}
	}
}