package com.metallicus.protonsdk

import android.content.Context
import com.metallicus.protonsdk.common.SecureKeys
import com.metallicus.protonsdk.common.Prefs
import com.metallicus.protonsdk.common.Resource
import com.metallicus.protonsdk.di.DaggerInjector
import com.metallicus.protonsdk.eosio.commander.digest.Sha256
import com.metallicus.protonsdk.eosio.commander.ec.EosPrivateKey
import com.metallicus.protonsdk.model.*
import com.metallicus.protonsdk.repository.AccountContactRepository
import com.metallicus.protonsdk.repository.AccountRepository
import com.metallicus.protonsdk.repository.ChainProviderRepository
import timber.log.Timber
import java.nio.charset.Charset
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
		return prefs.getActiveAccountName().isNotEmpty()
	}

	suspend fun fetchAccountsForKey(chainId: String, chainUrl: String, hyperionHistoryUrl: String, publicKey: String): Resource<List<Account>> {
		val accounts = mutableListOf<Account>()

		return try {
			val keyAccountResponse =
				accountRepository.fetchKeyAccount(hyperionHistoryUrl, publicKey)
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

	private suspend fun fetchAccount(chainId: String, chainUrl: String, accountName: String): Account? {
		var account: Account? = null

		val response = accountRepository.fetchAccount(chainUrl, accountName)
		if (response.isSuccessful) {
			response.body()?.let { it ->
				it.accountChainId = chainId
				it.accountContact = fetchAccountContact(chainUrl, accountName)

				account = it
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

		return account
	}

	private suspend fun addAccount(chainId: String, chainUrl: String, accountName: String) {
		val account = fetchAccount(chainId, chainUrl, accountName)

		requireNotNull(account)

		accountRepository.addAccount(account)
	}

	private suspend fun updateAccount(chainId: String, chainUrl: String, accountName: String): ChainAccount {
		val account = fetchAccount(chainId, chainUrl, accountName)

		requireNotNull(account)

		accountRepository.updateAccount(account)

		return accountRepository.getChainAccount(accountName)
	}

	suspend fun setActiveAccount(chainId: String, chainUrl: String, activeAccount: ActiveAccount): Resource<ChainAccount> {
		val publicKey = activeAccount.publicKey
		val accountName = activeAccount.accountName

		prefs.setActiveAccount(publicKey, accountName)

		return try {
			if (activeAccount.hasPrivateKey()) {
				secureKeys.addKey(publicKey, activeAccount.privateKey, activeAccount.pin)
			}

			addAccount(chainId, chainUrl, accountName)

			prefs.hasActiveAccount = true

			Resource.success(accountRepository.getChainAccount(accountName))
		} catch (e: Exception) {
			Resource.error(e.localizedMessage.orEmpty(), null)
		}
	}

	suspend fun getActiveAccount(): ChainAccount {
		val accountName = prefs.getActiveAccountName()
		return accountRepository.getChainAccount(accountName)
	}

	suspend fun refreshAccount(chainId: String, chainUrl: String, accountName: String): Resource<ChainAccount> {
		return try {
			Resource.success(updateAccount(chainId, chainUrl, accountName))
		} catch (e: Exception) {
			Resource.error(e.localizedMessage.orEmpty(), null)
		}
	}

	private fun getActiveAccountSignature(pin: String): String {
		val accountName = prefs.getActiveAccountName()
		val publicKey = prefs.getActivePublicKey()

		val privateKeyStr = secureKeys.getPrivateKey(publicKey, pin)
		val privateKey = EosPrivateKey(privateKeyStr)
		val sha256 = Sha256.from(accountName.toByteArray())
		val signature = privateKey.sign(sha256).toString()
		return signature
	}

	suspend fun updateAccountName(chainAccount: ChainAccount, pin: String, name: String): Resource<ChainAccount> {
		val signature = getActiveAccountSignature(pin)

		val accountName = chainAccount.account.accountName

		val response = accountRepository.updateAccountName(
			chainAccount.chainProvider.updateAccountNameUrl,
			accountName,
			signature,
			name)

		return if (response.isSuccessful) {
			val account = chainAccount.account
			account.accountContact.name = name
			accountRepository.updateAccount(account)

			Resource.success(chainAccount)
		} else {
			val msg = response.errorBody()?.string()
			val errorMsg = if (msg.isNullOrEmpty()) {
				response.message()
			} else {
				msg
			}
			Resource.error(errorMsg)
		}
	}

	suspend fun updateAccountAvatar(chainAccount: ChainAccount, pin: String, imageByteArray: ByteArray): Resource<ChainAccount> {
		val signature = getActiveAccountSignature(pin)

		val accountName = chainAccount.account.accountName

		val response = accountRepository.updateAccountAvatar(
			chainAccount.chainProvider.updateAccountAvatarUrl,
			accountName,
			signature,
			imageByteArray)

		return if (response.isSuccessful) {
			val account = chainAccount.account
			account.accountContact.avatar = imageByteArray.toString(Charset.defaultCharset())
			accountRepository.updateAccount(account)

			Resource.success(chainAccount)
		} else {
			val msg = response.errorBody()?.string()
			val errorMsg = if (msg.isNullOrEmpty()) {
				response.message()
			} else {
				msg
			}
			Resource.error(errorMsg)
		}
	}
}