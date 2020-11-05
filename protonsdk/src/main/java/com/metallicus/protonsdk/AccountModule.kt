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
package com.metallicus.protonsdk

import android.content.Context
import android.util.Base64
import com.google.gson.*
import com.greymass.esr.ESR
import com.greymass.esr.SigningRequest
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
import com.metallicus.protonsdk.repository.ESRRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Helper class used for [Account] based operations
 */
class AccountModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var accountRepository: AccountRepository

	@Inject
	lateinit var chainProviderRepository: ChainProviderRepository

	@Inject
	lateinit var accountContactRepository: AccountContactRepository

	@Inject
	lateinit var esrRepository: ESRRepository

	@Inject
	lateinit var prefs: Prefs

	@Inject
	lateinit var secureKeys: SecureKeys

	init {
		DaggerInjector.component.inject(this)
	}

	fun getActiveAccountName(): String {
		return prefs.getActiveAccountName()
	}

	suspend fun accountAvailable(chainUrl: String, accountName: String): Boolean {
		val response = accountRepository.fetchAccount(chainUrl, accountName)
		return if (response.isSuccessful) {
			false
		} else {
			// TODO: check for network errors
			true
		}
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

	fun hasPrivateKeys(): Boolean {
		return secureKeys.hasKeys()
	}

	fun resetPrivateKeys(oldPin: String, newPin: String): Boolean {
		return secureKeys.resetKeys(oldPin, newPin)
	}

	fun isPinValid(pin: String): Boolean {
		return secureKeys.checkPin(pin)
	}

	private suspend fun fetchAccountContact(chainUrl: String, accountName: String): AccountContact {
		val accountContact = AccountContact(accountName)
		accountContact.accountName = accountName

		val usersInfoTableScope = context.getString(R.string.usersInfoTableScope)
		val usersInfoTableCode = context.getString(R.string.usersInfoTableCode)
		val usersInfoTableName = context.getString(R.string.usersInfoTableName)

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

		val votersXPRInfoTableScope = context.getString(R.string.votersXPRInfoTableScope)
		val votersXPRInfoTableCode = context.getString(R.string.votersXPRInfoTableCode)
		val votersXPRInfoTableName = context.getString(R.string.votersXPRInfoTableName)

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
		val refundsXPRInfoTableCode = context.getString(R.string.refundsXPRInfoTableCode)
		val refundsXPRInfoTableName = context.getString(R.string.refundsXPRInfoTableName)

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

	private suspend fun fetchAccount(chainId: String, chainUrl: String, accountName: String): Account? {
		var account: Account? = null

		val response = accountRepository.fetchAccount(chainUrl, accountName)
		if (response.isSuccessful) {
			response.body()?.let { it ->
				it.accountChainId = chainId

				// fetch contact info
				it.accountContact = fetchAccountContact(chainUrl, accountName)

				// fetch voter info
				it.votersXPRInfo = fetchAccountVotersXPRInfo(chainUrl, accountName)

				// fetch refund info
				it.refundsXPRInfo = fetchAccountRefundsXPRInfo(chainUrl, accountName)

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

		requireNotNull(account) { "$accountName Not Found" }

		accountRepository.addAccount(account)
	}

	private suspend fun updateAccount(chainId: String, chainUrl: String, accountName: String): ChainAccount {
		val account = fetchAccount(chainId, chainUrl, accountName)

		requireNotNull(account) { "$accountName Not Found" }

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
			Resource.error(e.localizedMessage.orEmpty())
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
			Resource.error(e.localizedMessage.orEmpty())
		}
	}

	fun getActiveAccountPrivateKey(pin: String): String {
		val publicKey = prefs.getActivePublicKey()
		return secureKeys.getPrivateKey(publicKey, pin).orEmpty()
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

		val updateAccountNameUrl =
			chainAccount.chainProvider.chainApiUrl + chainAccount.chainProvider.updateAccountNamePath

		val response = accountRepository.updateAccountName(
			updateAccountNameUrl,
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

		val updateAccountAvatarUrl =
			chainAccount.chainProvider.chainApiUrl + chainAccount.chainProvider.updateAccountAvatarPath

		val response = accountRepository.updateAccountAvatar(
			updateAccountAvatarUrl,
			accountName,
			signature,
			imageByteArray)

		return if (response.isSuccessful) {
			val account = chainAccount.account
			account.accountContact.avatar = Base64.encodeToString(imageByteArray, Base64.DEFAULT)
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

	suspend fun decodeESR(chainAccount: ChainAccount, originalESRUrl: String): ProtonESR {
		val originalESRUrlScheme = originalESRUrl.substringBefore(":")
		val esrUrl = "esr:" + originalESRUrl.substringAfter(":")

		val chainId = chainAccount.chainProvider.chainId
		val chainUrl = chainAccount.chainProvider.chainUrl

		val esr = ESR(context) { account ->
			val response = chainProviderRepository.getAbi(chainUrl, account)
			if (response.isSuccessful) {
				response.body()?.toString()
			} else {
				response.errorBody()?.toString()
			}
		}

		val signingRequest = SigningRequest(esr)
		signingRequest.load(esrUrl)

		// TODO: need chainId original string from esr request
		//val esrChainId = signingRequest.chainId.toVariant()
		//if (esrChainId == chainAccount.chainProvider.chainId) {

		val requestAccountName = signingRequest.info["req_account"].orEmpty()

		val requestKey = ""
		if (signingRequest.isIdentity) {
			val linkStr = signingRequest.info["link"]

			// TODO: deserialize link as LinkCreate obj
		}

		val returnPath = signingRequest.info["return_path"].orEmpty()

		val requestAccount: Account? = if (requestAccountName.isNotEmpty()) {
			fetchAccount(chainId, chainUrl, requestAccountName)
		} else {
			null
		}

		return ProtonESR(
			requestKey,
			chainAccount,
			signingRequest,
			originalESRUrlScheme,
			requestAccount,
			returnPath
		)
	}

	suspend fun cancelAuthorizeESR(protonESR: ProtonESR): Resource<String> {
		return try {
			val callback = protonESR.signingRequest.callback

			val response =
				esrRepository.cancelAuthorizeESR(callback, "User canceled request")
			if (response.isSuccessful) {
				Resource.success(response.body())
			} else {
				val msg = response.errorBody()?.string()
				val errorMsg = if (msg.isNullOrEmpty()) {
					response.message()
				} else {
					msg
				}

				Resource.error(errorMsg)
			}
		} catch (e: Exception) {
			Resource.error(e.localizedMessage.orEmpty())
		}
	}

	suspend fun authorizeESR(protonESR: ProtonESR): Resource<JsonObject> {
		return try {
			val callback = protonESR.signingRequest.callback

			val response =
				esrRepository.authorizeESR(callback, "")
			if (response.isSuccessful) {
				Resource.success(response.body())
			} else {
				val msg = response.errorBody()?.string()
				val errorMsg = if (msg.isNullOrEmpty()) {
					response.message()
				} else {
					msg
				}

				Resource.error(errorMsg)
			}
		} catch (e: Exception) {
			Resource.error(e.localizedMessage.orEmpty())
		}
	}
}