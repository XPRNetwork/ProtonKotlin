/*
 * Copyright (c) 2021 Proton Chain LLC, Delaware
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
import android.net.Uri
import android.util.Base64
import com.google.common.primitives.Longs
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.greymass.esr.ESR
import com.greymass.esr.SigningRequest
import com.greymass.esr.models.PermissionLevel
import com.greymass.esr.models.TransactionContext
import com.metallicus.protonsdk.common.Prefs
import com.metallicus.protonsdk.common.Resource
import com.metallicus.protonsdk.common.SecureKeys
import com.metallicus.protonsdk.di.DaggerInjector
import com.metallicus.protonsdk.eosio.commander.BitUtils
import com.metallicus.protonsdk.eosio.commander.HexUtils
import com.metallicus.protonsdk.eosio.commander.digest.Sha256
import com.metallicus.protonsdk.eosio.commander.digest.Sha512
import com.metallicus.protonsdk.eosio.commander.ec.EosPrivateKey
import com.metallicus.protonsdk.eosio.commander.ec.EosPublicKey
import com.metallicus.protonsdk.eosio.commander.model.types.TypeChainId
import com.metallicus.protonsdk.model.*
import com.metallicus.protonsdk.repository.AccountContactRepository
import com.metallicus.protonsdk.repository.AccountRepository
import com.metallicus.protonsdk.repository.ChainProviderRepository
import com.metallicus.protonsdk.repository.ESRRepository
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.math.BigInteger
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
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

	suspend fun getAccountNamesForPublicKey(hyperionHistoryUrl: String, publicKey: String): List<String> {
		return try {
			val keyAccountResponse = accountRepository.fetchKeyAccount(hyperionHistoryUrl, publicKey)
			if (keyAccountResponse.isSuccessful) {
				keyAccountResponse.body()?.accountNames ?: emptyList()
			} else {
				emptyList()
			}
		} catch (e: Exception) {
			emptyList()
		}
	}

	suspend fun fetchAccountsForKey(chainId: String, chainUrl: String, hyperionHistoryUrl: String, publicKey: String): Resource<List<Account>> {
		return try {
			val accountNames = getAccountNamesForPublicKey(hyperionHistoryUrl, publicKey)
			if (accountNames.isNotEmpty()) {
				val accounts = mutableListOf<Account>()

				accountNames.forEach { accountName ->
					fetchAccount(chainId, chainUrl, accountName)?.let { account ->
						accounts.add(account)
					}
				}

				Resource.success(accounts)
			} else {
				Resource.error("No Account Names For Key")
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

	private suspend fun fetchAccountContact(chainId: String, chainUrl: String, accountName: String): AccountContact {
		val accountContact = AccountContact(accountName)
		accountContact.accountName = accountName

		val usersInfoTableScope = context.getString(R.string.usersInfoTableScope)
		val usersInfoTableCode = context.getString(R.string.usersInfoTableCode)
		val usersInfoTableName = context.getString(R.string.usersInfoTableName)

		val chainProvider = chainProviderRepository.getChainProvider(chainId)

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

				val verifiedFields = mutableListOf<String>()

				if (userInfo?.has("kyc") == true) {
					val kyc = userInfo.get("kyc")?.asJsonArray
					kyc?.forEach { kycJsonElement ->
						val kycRow = kycJsonElement.asJsonObject
						val kycProvider = kycRow.get("kyc_provider").asString
						if (chainProvider.kycProviders.find { it.kycProvider == kycProvider } != null) {
							val kycLevel = kycRow.get("kyc_level").asString

							val kycFields = kycLevel.split(",")
							kycFields.forEach { kycField ->
								val verifiedField = kycField.split(":")[1]

								verifiedFields.add(verifiedField)
							}
						}
					}
				}

				// make sure there are no duplicates
				verifiedFields.distinct()

				accountContact.verifiedFields = verifiedFields
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
				it.accountContact = fetchAccountContact(chainId, chainUrl, accountName)

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

	suspend fun setActiveAccount(chainId: String, chainUrl: String, activeAccount: ActiveAccount): Resource<ChainAccount> {
		val publicKey = activeAccount.getPublicKey()
		return if (publicKey.isNotEmpty()) {
			val accountName = activeAccount.accountName

			prefs.setActiveAccount(publicKey, accountName)

			try {
				if (activeAccount.hasPrivateKey()) {
					secureKeys.addKey(publicKey, activeAccount.privateKey, activeAccount.pin)
				}

				addAccount(chainId, chainUrl, accountName)

				prefs.hasActiveAccount = true

				Resource.success(accountRepository.getChainAccount(accountName))
			} catch (e: Exception) {
				Resource.error(e.localizedMessage.orEmpty())
			}
		} else {
			Resource.error("Invalid private key length or format")
		}
	}

	suspend fun getActiveAccount(): ChainAccount {
		val accountName = prefs.getActiveAccountName()
		return accountRepository.getChainAccount(accountName)
	}

	suspend fun refreshAccount(chainId: String, chainUrl: String, accountName: String): Resource<ChainAccount> {
		return try {
			val account = fetchAccount(chainId, chainUrl, accountName)

			requireNotNull(account) { "$accountName Not Found" }

			accountRepository.updateAccount(account)

			val chainAccount = accountRepository.getChainAccount(accountName)

			Resource.success(chainAccount)
		} catch (e: Exception) {
			Resource.error(e.localizedMessage.orEmpty())
		}
	}

	fun getActiveAccountPrivateKey(pin: String): String {
		val publicKey = prefs.getActivePublicKey()
		return secureKeys.getPrivateKey(publicKey, pin).orEmpty()
	}

	private fun signActiveAccountName(pin: String): String {
		val accountName = prefs.getActiveAccountName()
		return signWithActiveKey(pin, accountName)
	}

	private fun signWithActiveKey(pin: String, data: String): String {
		return signWithActiveKey(pin, data.toByteArray())
	}

	private fun signWithActiveKey(pin: String, data: ByteArray): String {
		val publicKey = prefs.getActivePublicKey()
		val privateKeyStr = secureKeys.getPrivateKey(publicKey, pin)
		val privateKey = EosPrivateKey(privateKeyStr)
		val sha256 = Sha256.from(data)
		return privateKey.sign(sha256).toString()
	}

	suspend fun updateAccountName(chainAccount: ChainAccount, pin: String, name: String): Resource<ChainAccount> {
		val signature = signActiveAccountName(pin)

		val accountName = chainAccount.account.accountName

		val updateAccountNameUrl =
			chainAccount.chainProvider.protonChainUrl + chainAccount.chainProvider.updateAccountNamePath

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
		val signature = signActiveAccountName(pin)

		val accountName = chainAccount.account.accountName

		val updateAccountAvatarUrl =
			chainAccount.chainProvider.protonChainUrl + chainAccount.chainProvider.updateAccountAvatarPath

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

	private suspend fun newSigningRequest(chainUrl: String): SigningRequest {
		val esr = ESR(context) { account ->
			runBlocking {
				val response = chainProviderRepository.getAbi(chainUrl, account)
				if (response.isSuccessful) {
					val abiJson = response.body()?.get("abi")
					abiJson?.toString().orEmpty()
				} else {
					response.errorBody()?.toString().orEmpty()
				}
			}
		}
		return SigningRequest(esr)
	}

	val disallowedESRActions = listOf(
		"updateauth",
		"deleteauth",
		"linkauth",
		"unlinkauth",
		"setabi",
		"setcode",
		"newaccount"
	)

	suspend fun decodeESR(chainAccount: ChainAccount, tokenContractMap: Map<String, TokenContract>, originalESRUrl: String, esrSession: ESRSession?=null): ProtonESR {
		val originalESRUrlScheme = originalESRUrl.substringBefore(":")
		val esrUrl = "esr:" + originalESRUrl.substringAfter(":")

		val chainId = chainAccount.chainProvider.chainId
		val chainUrl = chainAccount.chainProvider.chainUrl

		val signingRequest = newSigningRequest(chainUrl)
		signingRequest.load(esrUrl)

		// TODO: need chainId original string from esr request
		//val esrChainId = signingRequest.chainId.toVariant()
		//if (esrChainId == chainAccount.chainProvider.chainId) {

		val returnPath = signingRequest.info["return_path"].orEmpty()

		var requestAccount: Account? = null
		var requestKey = ""

		if (esrSession != null) {
			requestAccount = esrSession.requester
			requestKey = esrSession.id
		}

		val actions = mutableListOf<ESRAction>()
		if (signingRequest.isIdentity) {
			val requestAccountName = signingRequest.info["req_account"].orEmpty()

			requestAccount = fetchAccount(chainId, chainUrl, requestAccountName)

			val linkHexValue = signingRequest.infoPairs.find {
				it.key == "link"
			}?.hexValue.orEmpty()

			val linkCreate = signingRequest.decodeLinkCreate(linkHexValue)
			requestKey = linkCreate.requestKey
		} else {
			val resolvedActions = signingRequest.resolveActions()
			resolvedActions.forEach {
				val name = it.name.name
				if (!disallowedESRActions.contains(name)) {
					val accountName = it.account.name
					val data = it.data.data

					val type = if (name == "transfer") Type.TRANSFER else Type.CUSTOM

					val tokenContract = if (type == Type.TRANSFER) {
						val quantity = data["quantity"] as String
						if (quantity.isNotEmpty()) {
							val symbol = quantity.split(" ")[1]

							tokenContractMap["$accountName:$symbol"]
						} else {
							null
						}
					} else {
						null
					}

					val esrAction = ESRAction(type, name, accountName, data, tokenContract)
					actions.add(esrAction)
				}
			}
		}

		return ProtonESR(
			chainAccount,
			signingRequest,
			originalESRUrlScheme,
			requestAccount,
			returnPath,
			requestKey,
			actions
		)
	}

	suspend fun cancelAuthorizeESR(protonESR: ProtonESR): Resource<String> {
		return try {
			val callback = protonESR.signingRequest.callback

			val response =
				esrRepository.cancelAuthorizeESR(callback, "User canceled request")
			if (response.isSuccessful) {
				Resource.success(protonESR.returnPath)
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

	fun getAppName(): String {
		val applicationInfo = context.applicationInfo
		val stringId = applicationInfo.labelRes
		return if (stringId == 0) {
			applicationInfo.nonLocalizedLabel.toString()
		} else {
			context.getString(stringId)
		}
	}

	suspend fun authorizeESR(pin: String, protonESR: ProtonESR): Resource<String> {
		return try {
			requireNotNull(protonESR.requestKey)

			val resolvedSigningRequest =
				protonESR.signingRequest.resolve(
					PermissionLevel(protonESR.signingAccount.account.accountName, "active"), TransactionContext())

//			protonESR.resolvedSigningRequest = resolvedSigningRequest

			val chainIdStr = protonESR.signingAccount.chainProvider.chainId
			val chainIdByteArray = TypeChainId(chainIdStr).bytes

			val transactionByteArray = resolvedSigningRequest.serializedTransaction

			val trailingByteArray = ByteArray(32)

			val unsignedTransactionDigest = chainIdByteArray + transactionByteArray + trailingByteArray

			val signature = signWithActiveKey(pin, unsignedTransactionDigest)

			val callback = resolvedSigningRequest.getCallback(listOf(signature))

			val sessionKey = EosPrivateKey()

			val sessionChannel = Uri.Builder()
				.scheme("https")
				.authority("cb.anchor.link")
				.appendPath(UUID.randomUUID().toString())
				.build()

			val linkName = getAppName()

			val authParams = callback.payload
			authParams["link_key"] = sessionKey.publicKey.toString()
			authParams["link_ch"] = sessionChannel.toString()
			authParams["link_name"] = linkName
			//authParams.remove("SIG")
			authParams["sig"] = signature

			val originalESRUrlScheme = protonESR.originESRUrlScheme + ":"
			val req = authParams["req"]?.replace("esr:", originalESRUrlScheme)
			authParams["req"] = req

			val response = esrRepository.authorizeESR(callback.url, authParams)
			if (response.isSuccessful) {
				val createdAt = Date().time

				val esrSession = ESRSession(
					id = protonESR.requestKey,
					signer = protonESR.signingAccount.account.accountName,
					callbackUrl = callback.url,
					receiveKey = sessionKey.toWif(),
					receiveChannelUrl = sessionChannel.toString(),
					createdAt = createdAt,
					updatedAt = createdAt,
					requester = protonESR.requestAccount
				)

				esrRepository.addESRSession(esrSession)

				Resource.success(protonESR.returnPath)
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

	suspend fun getESRSessions(): List<ESRSession> {
		return esrRepository.getESRSessions()
	}

	suspend fun updateESRSession(esrSession: ESRSession) {
		esrRepository.updateESRSession(esrSession)
	}

	suspend fun removeESRSession(esrSession: ESRSession) {
		esrRepository.removeESRSession(esrSession)
	}

	suspend fun removeAllESRSessions() {
		esrRepository.removeAllESRSessions()
	}

	@Throws(IllegalArgumentException::class)
	suspend fun decodeESRSessionMessage(chainAccount: ChainAccount, tokenContractMap: Map<String, TokenContract>, esrSession: ESRSession, message: String): ProtonESR {
		//val chainId = chainAccount.chainProvider.chainId
		val chainUrl = chainAccount.chainProvider.chainUrl

		val sealedMessageSigningRequest = newSigningRequest(chainUrl)
		val sealedMessage = sealedMessageSigningRequest.decodeSealedMessage(message)

		val sealedPublicKey = EosPublicKey(sealedMessage.from)

		val sharedSecret = EosPrivateKey(esrSession.receiveKey).getSharedSecret(sealedPublicKey)

		val nonceLong = sealedMessage.nonce.toLong()
		val nonceByteArray = Longs.toByteArray(nonceLong).reversedArray()

		val symmetricKey = nonceByteArray + sharedSecret
		val symmetricSha512 = Sha512.from(symmetricKey).bytes

		val key = symmetricSha512.copyOfRange(0, 32)
		val iv = symmetricSha512.copyOfRange(32, 48)

		val cipherTextByteArray = sealedMessage.cipherText.hexStringToByteArray()

		val esrByteArray: ByteArray = try {
			cipherTextByteArray.aesDecrypt(key, iv)
		} catch (e: Exception) {
			Timber.e(e)
			byteArrayOf(0)
		}

		require(esrByteArray.isNotEmpty())

		val esrUrl = String(esrByteArray)

		val protonESR = decodeESR(chainAccount, tokenContractMap, esrUrl, esrSession)

		return protonESR
	}

	suspend fun authorizeESRActions(pin: String, protonESR: ProtonESR): Resource<String> {
		return try {
			requireNotNull(protonESR.requestKey)

			val chainUrl = protonESR.signingAccount.chainProvider.chainUrl

			val chainInfoResponse = chainProviderRepository.getChainInfo(chainUrl)
			if (chainInfoResponse.isSuccessful) {
				chainInfoResponse.body()?.let {
					val transactionContext = TransactionContext()
					transactionContext.refBlockNum =
						BigInteger(1, HexUtils.toBytes(it.lastIrreversibleBlockId.substring(0, 8))).toLong().and(0xFFFF)
					transactionContext.refBlockPrefix =
						BitUtils.uint32ToLong(HexUtils.toBytes(it.lastIrreversibleBlockId.substring(16, 24)), 0).and(0xFFFFFFFF)
					transactionContext.expiration = it.getTimeAfterHeadBlockTime(60000)

					val resolvedSigningRequest =
						protonESR.signingRequest.resolve(
							PermissionLevel(protonESR.signingAccount.account.accountName, "active"), transactionContext)

					val chainIdStr = protonESR.signingAccount.chainProvider.chainId
					val chainIdByteArray = TypeChainId(chainIdStr).bytes

					val transactionByteArray = resolvedSigningRequest.serializedTransaction

					val trailingByteArray = ByteArray(32)

					val unsignedTransactionDigest = chainIdByteArray + transactionByteArray + trailingByteArray

					val signature = signWithActiveKey(pin, unsignedTransactionDigest)

					val callback = resolvedSigningRequest.getCallback(listOf(signature))

					val esrSession = esrRepository.getESRSession(protonESR.requestKey)

					val authParams = callback.payload
					authParams["link_key"] = EosPrivateKey(esrSession.receiveKey).publicKey.toString()
					authParams["link_ch"] = esrSession.receiveChannelUrl
					authParams["link_name"] = getAppName()
					//authParams.remove("SIG")
					authParams["sig"] = signature

					val originalESRUrlScheme = protonESR.originESRUrlScheme + ":"
					val req = authParams["req"]?.replace("esr:", originalESRUrlScheme)
					authParams["req"] = req

					val response = esrRepository.authorizeESR(callback.url, authParams)
					if (response.isSuccessful) {
						esrSession.updatedAt = Date().time
						esrRepository.updateESRSession(esrSession)

						Resource.success(protonESR.returnPath)
					} else {
						val msg = response.errorBody()?.string()
						val errorMsg = if (msg.isNullOrEmpty()) {
							response.message()
						} else {
							msg
						}

						Resource.error(errorMsg)
					}
				} ?: Resource.error("No Chain Info")
			} else {
				val msg = chainInfoResponse.errorBody()?.string()
				val errorMsg = if (msg.isNullOrEmpty()) {
					chainInfoResponse.message()
				} else {
					msg
				}

				Resource.error(errorMsg)
			}
		} catch (e: Exception) {
			Resource.error(e.localizedMessage.orEmpty())
		}
	}

	private fun String.hexStringToByteArray(): ByteArray {
		val s = toUpperCase(Locale.ROOT)
		val len = s.length
		val data = ByteArray(len / 2)
		var i = 0
		while (i < len) {
			data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
			i += 2
		}
		return data
	}

	@Throws(
		NoSuchAlgorithmException::class,
		NoSuchPaddingException::class,
		InvalidKeyException::class,
		InvalidAlgorithmParameterException::class,
		IllegalBlockSizeException::class,
		BadPaddingException::class
	)
	private fun ByteArray.aesDecrypt(key: ByteArray, iv: ByteArray): ByteArray {
		val keySpec = SecretKeySpec(key, "AES")
		val ivSpec = IvParameterSpec(iv)
		val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
		return cipher.doFinal(this)
	}
}