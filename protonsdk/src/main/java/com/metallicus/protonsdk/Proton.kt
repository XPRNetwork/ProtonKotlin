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
import androidx.lifecycle.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.metallicus.protonsdk.api.TableRowsIndexPosition
import com.metallicus.protonsdk.common.*
import com.metallicus.protonsdk.di.DaggerInjector
import com.metallicus.protonsdk.di.ProtonModule
import com.metallicus.protonsdk.eosio.commander.digest.Sha256
import com.metallicus.protonsdk.eosio.commander.ec.EosPrivateKey
import com.metallicus.protonsdk.eosio.commander.model.chain.SignedTransaction
import com.metallicus.protonsdk.eosio.commander.model.chain.Action as ChainAction
import com.metallicus.protonsdk.model.*
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okio.ByteString
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Main class used for handling Proton Chain operations
 *
 * @param	context	[Context] context used for construction
 */
class Proton private constructor(context: Context) {
	init {
		if (BuildConfig.DEBUG) {
			Timber.plant(Timber.DebugTree())
		}

		DaggerInjector.buildComponent(context)
		DaggerInjector.component.inject(ProtonModule())
	}

	companion object : SingletonHolder<Proton, Context>(::Proton)

	private var workersModule: WorkersModule = WorkersModule()
	private var chainProviderModule: ChainProviderModule = ChainProviderModule()
	private var tokenContractsModule: TokenContractsModule = TokenContractsModule()
	private var accountModule: AccountModule = AccountModule()
	private var currencyBalancesModule: CurrencyBalancesModule = CurrencyBalancesModule()
	private var actionsModule: ActionsModule = ActionsModule()

	private val protonCoroutineScope = CoroutineScope(Dispatchers.Default)

	/**
	 * Initialization for [Proton]
	 *
	 * This should be the first function called. This will start the process of retrieving
	 * the [ChainProvider] and [TokenContract]s given a valid Proton Chain URL
	 *
	 * @param	protonChainUrl	Mainnet or Testnet Proton Chain Url
	 */
	fun initialize(protonChainUrl: String) {
		workersModule.init(protonChainUrl)
	}

	private suspend fun getChainProviderAsync() = suspendCoroutine<ChainProvider> { continuation ->
		workersModule.onInitChainProvider { success, data ->
			if (success) {
				protonCoroutineScope.launch {
					continuation.resume(chainProviderModule.getActiveChainProvider())
				}
			} else {
				continuation.resumeWithException(ProtonException(data))
			}
		}
	}

	/**
	 * Get the [ChainProvider] info
	 *
	 * This will return the [ChainProvider] info given the Proton Chain URl provided during initialization.
	 *
	 * @return	LiveData<Resource<[ChainProvider]>>
	 */
	fun getChainProvider(): LiveData<Resource<ChainProvider>> = liveData {
		emit(Resource.loading())

		try {
			val chainProvider = getChainProviderAsync()
			emit(Resource.success(chainProvider))
		} catch (e: ProtonException) {
			val error: Resource<ChainProvider> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<ChainProvider> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun setPreferredChainUrl(chainUrl: String): LiveData<Resource<Boolean>> = liveData {
		emit(Resource.loading())

		try {
			val chainProvider = getChainProviderAsync()

			chainProviderModule.updateChainUrl(chainProvider.chainId, chainUrl)

			emit(Resource.success(true))
		} catch (e: ProtonException) {
			val error: Resource<Boolean> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<Boolean> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun setPreferredHyperionHistoryUrl(hyperionHistoryUrl: String): LiveData<Resource<Boolean>> = liveData {
		emit(Resource.loading())

		try {
			val chainProvider = getChainProviderAsync()

			chainProviderModule.updateHyperionHistoryUrl(chainProvider.chainId, hyperionHistoryUrl)

			emit(Resource.success(true))
		} catch (e: ProtonException) {
			val error: Resource<Boolean> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<Boolean> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	private suspend fun getTokenContractsAsync() = suspendCoroutine<List<TokenContract>> { continuation ->
		workersModule.onInitTokenContracts { success, data ->
			if (success) {
				protonCoroutineScope.launch {
					continuation.resume(tokenContractsModule.getTokenContracts())
				}
			} else {
				continuation.resumeWithException(ProtonException(data))
			}
		}
	}

	private suspend fun getTokenContractsMapAsync(): Map<String, TokenContract> {
		val tokenContracts = getTokenContractsAsync()
		return tokenContracts.associateBy { "${it.contract}:${it.getSymbol()}" }
	}

	fun getTokenContracts(updateExchangeRates: Boolean = false): LiveData<Resource<List<TokenContract>>> = liveData {
		emit(Resource.loading())

		try {
			val tokenContracts = getTokenContractsAsync()

			if (updateExchangeRates) {
				val activeAccount = getActiveAccountAsync()

				val tokenContractsMap = tokenContracts.associateBy { "${it.contract}:${it.getSymbol()}" }

				val exchangeRateUrl =
					activeAccount.chainProvider.protonChainUrl + activeAccount.chainProvider.exchangeRatePath

				tokenContractsModule.updateExchangeRates(exchangeRateUrl, tokenContractsMap)
			}

			emit(Resource.success(tokenContracts))
		} catch (e: ProtonException) {
			val error: Resource<List<TokenContract>> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<List<TokenContract>> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun getTokenContractsMap(updateExchangeRates: Boolean = false): LiveData<Resource<Map<String, TokenContract>>> = liveData {
		emit(Resource.loading())

		try {
			val tokenContractsMap = getTokenContractsMapAsync()

			if (updateExchangeRates) {
				val activeAccount = getActiveAccountAsync()

				val exchangeRateUrl =
					activeAccount.chainProvider.protonChainUrl + activeAccount.chainProvider.exchangeRatePath

				tokenContractsModule.updateExchangeRates(exchangeRateUrl, tokenContractsMap)
			}

			emit(Resource.success(tokenContractsMap))
		} catch (e: ProtonException) {
			val error: Resource<Map<String, TokenContract>> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<Map<String, TokenContract>> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun generatePrivateKey(): Pair<String, String> {
		val privateKey = EosPrivateKey()
		val publicKeyStr = privateKey.publicKey.toString()
		val privateKeyStr = privateKey.toWif()
		return Pair<String, String>(publicKeyStr, privateKeyStr)
	}

	fun signWithPrivateKey(privateKeyStr: String, valueToSign: String): String {
		return try {
			val privateKey = EosPrivateKey(privateKeyStr)
			val sha256 = Sha256.from(valueToSign.toByteArray())
			privateKey.sign(sha256).toString()
		} catch (e: Exception) { "" }
	}

	fun hasPrivateKeys(): Boolean {
		return accountModule.hasPrivateKeys()
	}

	fun resetPrivateKeys(oldPin: String, newPin: String): Boolean {
		return accountModule.resetPrivateKeys(oldPin, newPin)
	}

	fun isPinValid(pin: String): Boolean {
		return accountModule.isPinValid(pin)
	}

	fun accountAvailable(accountName: String): LiveData<Resource<Boolean>> = liveData {
		emit(Resource.loading())

		try {
			val chainProvider = getChainProviderAsync()

			emit(Resource.success(accountModule.accountAvailable(chainProvider.chainUrl, accountName)))
		} catch (e: ProtonException) {
			val error: Resource<Boolean> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<Boolean> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	private suspend fun findAccounts(publicKeyStr: String): Resource<List<Account>> {
		return try {
			val chainProvider = getChainProviderAsync()

			accountModule.fetchAccountsForKey(
				chainProvider.chainId,
				chainProvider.chainUrl,
				chainProvider.hyperionHistoryUrl,
				publicKeyStr)
		} catch (e: ProtonException) {
			Resource.error(e)
		} catch (e: Exception) {
			Resource.error(e.localizedMessage.orEmpty())
		}
	}

	fun findAccountsForPublicKey(publicKeyStr: String): LiveData<Resource<List<Account>>> = liveData {
		emit(Resource.loading())

		emit(findAccounts(publicKeyStr))
	}

	fun findAccountsForPrivateKey(privateKeyStr: String): LiveData<Resource<List<Account>>> = liveData {
		emit(Resource.loading())

		try {
			val privateKey = EosPrivateKey(privateKeyStr)
			val publicKeyStr = privateKey.publicKey.toString()

			val accounts = findAccounts(publicKeyStr)
			emit(accounts)
		} catch (e: ProtonException) {
			val error: Resource<List<Account>> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<List<Account>> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun getActiveAccountName(): String {
		return accountModule.getActiveAccountName()
	}

	fun hasActiveAccount(): Boolean {
		return getActiveAccountName().isNotEmpty()
	}

	fun setActiveAccount(activeAccount: ActiveAccount, newAccount: Boolean=false): LiveData<Resource<ChainAccount>> = liveData {
		emit(Resource.loading())

		try {
			val chainProvider = getChainProviderAsync()

			if (newAccount) {
				emit(accountModule.setActiveAccount(chainProvider.chainId, chainProvider.chainUrl, activeAccount))
			} else {
				val publicKey = activeAccount.getPublicKey()
				if (publicKey.isNotEmpty()) {
					val accountNames = accountModule.getAccountNamesForPublicKey(chainProvider.hyperionHistoryUrl, publicKey)
					if (accountNames.contains(activeAccount.accountName)) {
						emit(accountModule.setActiveAccount(chainProvider.chainId, chainProvider.chainUrl, activeAccount))
					} else {
						val error: Resource<ChainAccount> = Resource.error("Please use the private key associated with '${activeAccount.accountName}'")
						emit(error)
					}
				} else {
					val error: Resource<ChainAccount> = Resource.error("Invalid private key length or format")
					emit(error)
				}
			}
		} catch (e: ProtonException) {
			val error: Resource<ChainAccount> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<ChainAccount> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun getActiveAccountPrivateKey(pin: String): String {
		return accountModule.getActiveAccountPrivateKey(pin)
	}

	private suspend fun getActiveAccountAsync() = suspendCoroutine<ChainAccount> { continuation ->
		workersModule.onInitActiveAccount { success, data ->
			if (success) {
				protonCoroutineScope.launch {
					continuation.resume(accountModule.getActiveAccount())
				}
			} else {
				continuation.resumeWithException(ProtonException(data))
			}
		}
	}

	fun getActiveAccount(): LiveData<Resource<ChainAccount>> = liveData {
		emit(Resource.loading())

		try {
			val activeAccount = getActiveAccountAsync()
			emit(Resource.success(activeAccount))
		} catch (e: ProtonException) {
			val error: Resource<ChainAccount> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<ChainAccount> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun refreshActiveAccount(): LiveData<Resource<ChainAccount>> = liveData {
		emit(Resource.loading())

		try {
			val activeAccount = getActiveAccountAsync()
			emit(accountModule.refreshAccount(
					activeAccount.chainProvider.chainId,
					activeAccount.chainProvider.chainUrl,
					activeAccount.account.accountName))
		} catch (e: ProtonException) {
			val error: Resource<ChainAccount> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<ChainAccount> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun getActiveAccountTokenBalances(addEmptyTokens: Boolean = false): LiveData<Resource<List<TokenCurrencyBalance>>> = liveData {
		emit(Resource.loading())

		try {
			val tokenContractsMap = getTokenContractsMapAsync()
			val activeAccount = getActiveAccountAsync()

			val exchangeRateUrl = activeAccount.chainProvider.protonChainUrl + activeAccount.chainProvider.exchangeRatePath

			tokenContractsModule.updateExchangeRates(exchangeRateUrl, tokenContractsMap)

			val tokenBalances =
				currencyBalancesModule.getTokenCurrencyBalances(
					activeAccount.chainProvider.hyperionHistoryUrl,
					activeAccount.account.accountName,
					tokenContractsMap,
					addEmptyTokens)

			emit(tokenBalances)
		} catch (e: ProtonException) {
			val error: Resource<List<TokenCurrencyBalance>> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<List<TokenCurrencyBalance>> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun getActiveAccountTokenBalance(tokenContractId: String): LiveData<Resource<TokenCurrencyBalance>> = liveData {
		emit(Resource.loading())

		try {
			val activeAccount = getActiveAccountAsync()

			val tokenContract = tokenContractsModule.getTokenContract(tokenContractId)

			val tokenBalance =
				currencyBalancesModule.getTokenCurrencyBalance(
					activeAccount.chainProvider.hyperionHistoryUrl,
					activeAccount.account.accountName,
					tokenContract)

			emit(tokenBalance)
		} catch (e: ProtonException) {
			val error: Resource<TokenCurrencyBalance> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<TokenCurrencyBalance> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	suspend fun getActiveAccountActions(contract: String, symbol: String, limit: Int, skip: Int): Resource<List<Action>> {
		return try {
			val activeAccount = getActiveAccountAsync()

			actionsModule.getActions(
				activeAccount.chainProvider.chainUrl,
				activeAccount.chainProvider.hyperionHistoryUrl,
				activeAccount.account.accountName,
				contract,
				symbol,
				limit,
				skip)
		} catch (e: ProtonException) {
			Resource.error(e)
		} catch (e: Exception) {
			Resource.error(e.localizedMessage.orEmpty())
		}
	}

	fun getActiveAccountActionsLiveData(contract: String, symbol: String, limit: Int, skip: Int): LiveData<Resource<List<Action>>> = liveData {
		emit(Resource.loading())

		emit(getActiveAccountActions(contract, symbol, limit, skip))
	}

	fun updateAccountName(pin: String, name: String): LiveData<Resource<ChainAccount>> = liveData {
		emit(Resource.loading())

		try {
			val activeAccount = getActiveAccountAsync()

			emit(accountModule.updateAccountName(activeAccount, pin, name))
		} catch (e: ProtonException) {
			val error: Resource<ChainAccount> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<ChainAccount> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun updateAccountAvatar(pin: String, byteArray: ByteArray): LiveData<Resource<ChainAccount>> = liveData {
		emit(Resource.loading())

		try {
			val activeAccount = getActiveAccountAsync()

			emit(accountModule.updateAccountAvatar(activeAccount, pin, byteArray))
		} catch (e: ProtonException) {
			val error: Resource<ChainAccount> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<ChainAccount> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun transferTokensByTokenContractId(pin: String, tokenContractId: String, toAccount: String, amount: String, memo: String): LiveData<Resource<JsonObject>> = liveData {
		emit(Resource.loading())

		try {
			val activeAccount = getActiveAccountAsync()

			val tokenContract = tokenContractsModule.getTokenContract(tokenContractId)

			emit(actionsModule.transferTokens(
				activeAccount.chainProvider.chainUrl,
				pin,
				tokenContract.contract,
				activeAccount.account.accountName,
				toAccount,
				amount,
				memo))
		} catch (e: ProtonException) {
			val error: Resource<JsonObject> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<JsonObject> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun transferTokensByContract(pin: String, contract: String, toAccount: String, amount: String, memo: String): LiveData<Resource<JsonObject>> = liveData {
		emit(Resource.loading())

		try {
			val activeAccount = getActiveAccountAsync()

			emit(actionsModule.transferTokens(
				activeAccount.chainProvider.chainUrl,
				pin,
				contract,
				activeAccount.account.accountName,
				toAccount,
				amount,
				memo))
		} catch (e: ProtonException) {
			val error: Resource<JsonObject> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<JsonObject> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	suspend fun signTransaction(pin: String, actions: List<ChainAction>): Resource<SignedTransaction> {
		val activeAccount = getActiveAccountAsync()

		return actionsModule.signTransaction(
			activeAccount.chainProvider.chainUrl,
			pin,
			actions)
	}

	fun signAndPushTransaction(pin: String, actions: List<ChainAction>): LiveData<Resource<JsonObject>> = liveData {
		emit(Resource.loading())

		try {
			val activeAccount = getActiveAccountAsync()

			emit(actionsModule.signAndPushTransaction(
				activeAccount.chainProvider.chainUrl,
				pin,
				actions))
		} catch (e: ProtonException) {
			val error: Resource<JsonObject> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<JsonObject> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun decodeESR(esrUri: String): LiveData<Resource<ProtonESR>> = liveData {
		emit(Resource.loading())

		try {
			val activeAccount = getActiveAccountAsync()

			val tokenContracts = getTokenContractsAsync()
			val tokenContractMap = tokenContracts.associateBy { "${it.contract}:${it.getSymbol()}" }

			emit(Resource.success(accountModule.decodeESR(activeAccount, tokenContractMap, esrUri)))
		} catch (e: ProtonException) {
			val error: Resource<ProtonESR> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<ProtonESR> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun cancelAuthorizeESR(protonESR: ProtonESR): LiveData<Resource<String>> = liveData {
		emit(Resource.loading())

		try {
			emit(accountModule.cancelAuthorizeESR(protonESR))
		} catch (e: ProtonException) {
			val error: Resource<String> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<String> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun authorizeESR(pin: String, protonESR: ProtonESR): LiveData<Resource<String>> = liveData {
		emit(Resource.loading())

		try {
			val returnPath = accountModule.authorizeESR(pin, protonESR)
			initESRSessions()
			emit(returnPath)
		} catch (e: ProtonException) {
			val error: Resource<String> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<String> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	private class ESRSessionListener(
		val onOpenCallback: (WebSocket, String, Int) -> Unit,
		val onMessageCallback: (String) -> Unit,
		val onClosingCallback: (String, Int) -> Unit,
		val onClosedCallback: (String, Int) -> Unit,
		val onFailureCallback: (String, Int) -> Unit
	): WebSocketListener() {
		override fun onOpen(webSocket: WebSocket, response: Response) {
			super.onOpen(webSocket, response)
			Timber.d("ESRWebSocketListener onOpen - ${response.message}")
			onOpenCallback(webSocket, response.message, response.code)
		}

		override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
			super.onMessage(webSocket, bytes)
			val text = bytes.hex()
			Timber.d("ESRWebSocketListener onMessage - $text")
			onMessageCallback(text)
		}

		override fun onMessage(webSocket: WebSocket, text: String) {
			super.onMessage(webSocket, text)
			Timber.d("ESRWebSocketListener onMessage - $text")
			onMessageCallback(text)
		}

		override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
			super.onClosing(webSocket, code, reason)
			Timber.d("ESRWebSocketListener onClosing - $reason")
			onClosingCallback(reason, code)
		}

		override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
			super.onClosed(webSocket, code, reason)
			Timber.d("ESRWebSocketListener onClosed - $reason")
			onClosedCallback(reason, code)
		}

		override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
			super.onFailure(webSocket, t, response)
			val message = response?.message.orEmpty()
			val code = response?.code ?: 0
			Timber.d("ESRWebSocketListener onFailure - ${response?.message} : ${t.localizedMessage}")
			onFailureCallback(message, code)
		}
	}

	private val openESRSessions = mutableListOf<Pair<String, WebSocket>>()
	private fun getOpenESRSession(id: String): Pair<String, WebSocket>? {
		return openESRSessions.find { it.first == id }
	}
	private fun isESRSessionOpen(id: String): Boolean {
		return getOpenESRSession(id) != null
	}
	private fun addOpenESRSession(id: String, webSocket: WebSocket) {
		if (!isESRSessionOpen(id)) {
			openESRSessions.add(Pair(id, webSocket))
		} else {
			webSocket.cancel()
		}
	}
	private fun removeOpenESRSession(id: String) {
		getOpenESRSession(id)?.let {
			it.second.cancel() // or close(1000, id)?
			openESRSessions.remove(it)
		}
	}
	private fun removeAllOpenESRSessions() {
		openESRSessions.forEach {
			it.second.cancel() // or close(1000, id)?
		}
		openESRSessions.clear()
	}

	val esrSessionMessages = MutableLiveData<MutableList<ESRSessionMessage>>(mutableListOf())
	private fun esrSessionMessageExists(message: String): Boolean {
		return esrSessionMessages.value?.find { it.message == message } != null
	}
	private fun addESRSessionMessage(esrSessionMessage: ESRSessionMessage) {
		if (!esrSessionMessageExists(esrSessionMessage.message)) {
			val list = esrSessionMessages.value
			list?.apply {
				add(esrSessionMessage)
				esrSessionMessages.postValue(this)
			}
		}
	}
	private fun removeESRSessionMessage(esrSessionMessage: ESRSessionMessage) {
		val list = esrSessionMessages.value
		list?.apply {
			remove(esrSessionMessage)
			esrSessionMessages.postValue(this)
		}
	}
	private fun removeESRSessionMessages(esrSessionId: String) {
		esrSessionMessages.value?.forEach {
			if (it.esrSession.id == esrSessionId) {
				removeESRSessionMessage(it)
			}
		}
	}
	private fun removeAllESRSessionMessages() {
		esrSessionMessages.value?.forEach {
			removeESRSessionMessage(it)
		}
	}

	fun initESRSessions(/*activeAccount*/) = protonCoroutineScope.launch {
		try {
			val esrSessionList = accountModule.getESRSessions(/*activeAccount*/)
			esrSessionList.forEach { esrSession ->
				val esrSessionId = esrSession.id
				if (!isESRSessionOpen(esrSessionId)) {
					val request = Request.Builder().url(esrSession.receiveChannelUrl).build()

					val logging = HttpLoggingInterceptor()
					logging.level = HttpLoggingInterceptor.Level.BODY

					val httpClient = OkHttpClient.Builder()
						.callTimeout(30, TimeUnit.SECONDS)
						.connectTimeout(30, TimeUnit.SECONDS)
						.readTimeout(30, TimeUnit.SECONDS)
						.writeTimeout(30, TimeUnit.SECONDS)
						.addInterceptor(logging)
						.build()

					httpClient.newWebSocket(request, ESRSessionListener(
						onOpenCallback = { webSocket, message, _ ->
							Timber.d("ESR Listener onOpen - $message")

							addOpenESRSession(esrSessionId, webSocket)
						},
						onMessageCallback = { message ->
							Timber.d("ESR Listener onMessage - $message")

							addESRSessionMessage(ESRSessionMessage(esrSession, message))
						},
						onClosingCallback = { reason, _ ->
							Timber.d("ESR Listener onClosing - $reason")
						},
						onClosedCallback = { reason, _ ->
							Timber.d("ESR Listener onClosed - $reason")

							removeOpenESRSession(esrSessionId)
						},
						onFailureCallback = { message, _ ->
							Timber.d("ESR Listener onFailure - $message")
						}
					))
				}
			}
		} catch (e: Exception) {
			Timber.e(e)
		}
	}

	fun decodeESRSessionMessage(esrSessionMessage: ESRSessionMessage): LiveData<Resource<ProtonESR>> = liveData {
		emit(Resource.loading())

		try {
			val activeAccount = getActiveAccountAsync()

			val tokenContracts = getTokenContractsAsync()
			val tokenContractMap = tokenContracts.associateBy { "${it.contract}:${it.getSymbol()}" }

			val esrSession = esrSessionMessage.esrSession
			val message = esrSessionMessage.message

			emit(Resource.success(accountModule.decodeESRSessionMessage(activeAccount, tokenContractMap, esrSession, message)))
		} catch (e: ProtonException) {
			removeESRSessionMessage(esrSessionMessage)

			val error: Resource<ProtonESR> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			removeESRSessionMessage(esrSessionMessage)

			val error: Resource<ProtonESR> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun cancelESRSessionMessage(esrSessionMessage: ESRSessionMessage, protonESR: ProtonESR): LiveData<Resource<String>> = liveData {
		emit(Resource.loading())

		try {
			val response = accountModule.cancelAuthorizeESR(protonESR)

			removeESRSessionMessage(esrSessionMessage)

			emit(response)
		} catch (e: ProtonException) {
			removeESRSessionMessage(esrSessionMessage)

			val error: Resource<String> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			removeESRSessionMessage(esrSessionMessage)

			val error: Resource<String> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun authorizeESRSessionMessage(pin: String, esrSessionMessage: ESRSessionMessage, protonESR: ProtonESR): LiveData<Resource<String>> = liveData {
		emit(Resource.loading())

		try {
			val response = accountModule.authorizeESRActions(pin, protonESR)

			removeESRSessionMessage(esrSessionMessage)

			emit(response)
		} catch (e: ProtonException) {
			removeESRSessionMessage(esrSessionMessage)

			val error: Resource<String> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			removeESRSessionMessage(esrSessionMessage)

			val error: Resource<String> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun getESRSessions(): LiveData<Resource<List<ESRSession>>> = liveData {
		emit(Resource.loading())

		try {
			@Suppress("UNUSED_VARIABLE")
			val activeAccount = getActiveAccountAsync()

			val esrSessions = accountModule.getESRSessions(/*activeAccount*/)

			emit(Resource.success(esrSessions))
		} catch (e: ProtonException) {
			val error: Resource<List<ESRSession>> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<List<ESRSession>> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun removeESRSession(esrSession: ESRSession): LiveData<Resource<Boolean>> = liveData {
		emit(Resource.loading())

		try {
			@Suppress("UNUSED_VARIABLE")
			val activeAccount = getActiveAccountAsync()

			val esrSessionId = esrSession.id

			removeOpenESRSession(esrSessionId)
			removeESRSessionMessages(esrSessionId)

			accountModule.removeESRSession(esrSession)

			emit(Resource.success(true))
		} catch (e: ProtonException) {
			val error: Resource<Boolean> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<Boolean> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun removeAllESRSessions(): LiveData<Resource<Boolean>> = liveData {
		emit(Resource.loading())

		try {
			@Suppress("UNUSED_VARIABLE")
			val activeAccount = getActiveAccountAsync()

			removeAllOpenESRSessions()
			removeAllESRSessionMessages()

			accountModule.removeAllESRSessions()

			emit(Resource.success(true))
		} catch (e: ProtonException) {
			val error: Resource<Boolean> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<Boolean> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	fun getSwapPools(): LiveData<Resource<SwapPoolData>> = liveData {
		emit(Resource.loading())

		try {
			val tokenContractsMap = getTokenContractsMapAsync()
			val activeAccount = getActiveAccountAsync()

			val exchangeRateUrl = activeAccount.chainProvider.protonChainUrl + activeAccount.chainProvider.exchangeRatePath

			tokenContractsModule.updateExchangeRates(exchangeRateUrl, tokenContractsMap)

			val tokenBalancesResource =
				currencyBalancesModule.getTokenCurrencyBalances(
					activeAccount.chainProvider.hyperionHistoryUrl,
					activeAccount.account.accountName,
					tokenContractsMap,
					true)

			when (tokenBalancesResource.status) {
				Status.SUCCESS -> {
					tokenBalancesResource.data?.let { tokenCurrencyBalances ->
						val tokenCurrencyBalancesMap = tokenCurrencyBalances.associateBy {
							"${it.tokenContract.contract}:${it.tokenContract.getSymbol()}"
						}

						val tableRowsResource = getTableRows(
							"proton.swaps",
							"proton.swaps",
							"pools",
							"",
							"",
							250//,
							//TableRowsIndexPosition.SECONDARY.indexPositionName
						)
						when (tableRowsResource.status) {
							Status.SUCCESS -> {
								val swapPools = mutableListOf<SwapPool>()
								val swapPoolTokens = mutableListOf<TokenCurrencyBalance>()

								val gson = Gson()

								tableRowsResource.data?.let { tableJson ->
									val rows = tableJson.get("rows").asJsonArray
									rows.forEach {
										val swapPoolJsonObject = it.asJsonObject

										val swapPool = gson.fromJson(swapPoolJsonObject, SwapPool::class.java)
										if (swapPool.active == 1) {
											val pool1 = "${swapPool.getPool1Contract()}:${swapPool.getPool1Symbol()}"
											val pool2 = "${swapPool.getPool2Contract()}:${swapPool.getPool2Symbol()}"

											tokenCurrencyBalancesMap[pool1]?.let { tokenCurrencyBalance ->
												swapPoolTokens.add(tokenCurrencyBalance)
											}
											tokenCurrencyBalancesMap[pool2]?.let { tokenCurrencyBalance ->
												swapPoolTokens.add(tokenCurrencyBalance)
											}

											swapPools.add(swapPool)
										}
									}
								}

								val swapPoolData = SwapPoolData(swapPools, swapPoolTokens.distinct())

								emit(Resource.success(swapPoolData))
							}
							Status.ERROR -> {
								val error: Resource<SwapPoolData> =
									Resource.error(tableRowsResource.message.orEmpty(), tableRowsResource.code ?: -1)
								emit(error)
							}
							Status.LOADING -> { }
						}
					}
				}
				Status.ERROR -> {
					val error: Resource<SwapPoolData> =
						Resource.error(tokenBalancesResource.message.orEmpty(), tokenBalancesResource.code ?: -1)
					emit(error)
				}
				Status.LOADING -> { }
			}
		} catch (e: ProtonException) {
			val error: Resource<SwapPoolData> = Resource.error(e)
			emit(error)
		} catch (e: Exception) {
			val error: Resource<SwapPoolData> = Resource.error(e.localizedMessage.orEmpty())
			emit(error)
		}
	}

	suspend fun getTableRows(
		scope: String,
		code: String,
		name: String,
		lowerBound: String = "",
		upperBound: String = "",
		limit: Long = 10,
		indexPosition: String = TableRowsIndexPosition.PRIMARY.indexPositionName,
		reverse: Boolean = false
	): Resource<JsonObject> {
		val chainProvider = getChainProviderAsync()

		return chainProviderModule.getTableRows(
			chainProvider.chainUrl,
			scope,
			code,
			name,
			lowerBound,
			upperBound,
			limit,
			indexPosition,
			reverse)
	}
}