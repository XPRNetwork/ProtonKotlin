package com.metallicus.protonsdk

import android.content.Context
import com.metallicus.protonsdk.common.SecureKeys
import com.metallicus.protonsdk.common.Prefs
import com.metallicus.protonsdk.common.Resource
import com.metallicus.protonsdk.di.DaggerInjector
import com.metallicus.protonsdk.eosio.commander.ec.EosPrivateKey
import com.metallicus.protonsdk.model.*
import com.metallicus.protonsdk.repository.AccountContactRepository
import com.metallicus.protonsdk.repository.AccountRepository
import com.metallicus.protonsdk.repository.ChainProviderRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import timber.log.Timber
import javax.inject.Inject

class AccountModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var chainProviderRepository: ChainProviderRepository

	@Inject
	lateinit var accountRepository: AccountRepository

	@Inject
	lateinit var accountContactRepository: AccountContactRepository

	@Inject
	lateinit var prefs: Prefs

	@Inject
	lateinit var secureKeys: SecureKeys

	init {
		DaggerInjector.component.inject(this)
	}

	fun hasActiveAccount(): Boolean {
		return prefs.activeAccountName.isNotEmpty()
	}

	suspend fun getAccountsForPrivateKey(chainId: String, chainUrl: String, hyperionHistoryUrl: String, privateKeyStr: String): Resource<List<ChainAccount>> {
		val accounts = mutableListOf<ChainAccount>()

		return try {
			val privateKey = EosPrivateKey(privateKeyStr)
			val publicKeyStr = privateKey.publicKey.toString()

			val keyAccountResponse =
				accountRepository.fetchKeyAccount(hyperionHistoryUrl, publicKeyStr)
			if (keyAccountResponse.isSuccessful) {
				keyAccountResponse.body()?.let { keyAccount ->
					keyAccount.accountNames.forEach { accountName ->
						fetchAccount(chainId, chainUrl, accountName)?.let { account ->
							accounts.add(account)
						}
					}
				}

				Resource.success(accounts)
			} else {
				val msg = keyAccountResponse.errorBody()?.string()
				val errorMsg = if (msg.isNullOrEmpty()) {
					keyAccountResponse.message()
				} else {
					msg
				}

				Resource.error(errorMsg)
			}
		} catch (e: Exception) {
			Timber.d(e)

			Resource.error(e.localizedMessage.orEmpty())
		}
	}

	private suspend fun fetchAccountContact(chainUrl: String, accountName: String): AccountContact {
		val accountContact = AccountContact(accountName)
		accountContact.accountName = accountName

		val usersInfoTableScope = context.getString(R.string.protonChainUsersInfoTableScope)
		val usersInfoTableCode = context.getString(R.string.protonChainUsersInfoTableCode)
		val usersInfoTableName = context.getString(R.string.protonChainUsersInfoTableName)

		val response = accountContactRepository.fetchAccountContact(
			chainUrl, accountName, usersInfoTableScope, usersInfoTableCode, usersInfoTableName)
		if (response.isSuccessful) {
			val userInfoJsonObject = response.body()

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

		return accountContact
	}

	private suspend fun fetchAccount(chainId: String, chainUrl: String, accountName: String): ChainAccount? {
		var chainAccount: ChainAccount? = null

		val response = accountRepository.fetchAccount(chainUrl, accountName)
		if (response.isSuccessful) {
			response.body()?.let { account ->
				account.accountChainId = chainId
				account.accountContact = fetchAccountContact(chainUrl, accountName)

				accountRepository.addAccount(account)

				chainAccount = accountRepository.getChainAccount(accountName)
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

		return chainAccount
	}

	fun setActiveAccount(account: ChainAccount, privateKeyStr: String, pin: String) {
		val privateKey = EosPrivateKey(privateKeyStr)
		val publicKeyStr = privateKey.publicKey.toString()
		secureKeys.addKey(publicKeyStr, privateKeyStr, pin)

		prefs.activeAccountName = account.account.accountName
		prefs.hasActiveAccount = true
	}

	suspend fun getActiveAccount(): ChainAccount {
		val accountName = prefs.activeAccountName
		return accountRepository.getChainAccount(accountName)
	}

	suspend fun refreshActiveAccount(): Resource<ChainAccount> {
		val chainId = prefs.activeChainId
		val accountName = prefs.activeAccountName

		return if (chainId.isNotEmpty() && accountName.isNotEmpty()) {
			val chainProvider = chainProviderRepository.getChainProvider(chainId)
			Resource.success(fetchAccount(chainId, chainProvider.chainUrl, accountName))
		} else {
			Resource.error("No Active Chain or Account")
		}
	}
}