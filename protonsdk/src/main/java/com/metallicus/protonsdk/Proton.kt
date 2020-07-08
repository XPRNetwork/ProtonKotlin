package com.metallicus.protonsdk

import android.content.Context
import androidx.lifecycle.*
import com.google.gson.JsonObject
import com.metallicus.protonsdk.common.ProtonException
import com.metallicus.protonsdk.common.Resource
import com.metallicus.protonsdk.common.SingletonHolder
import com.metallicus.protonsdk.di.DaggerInjector
import com.metallicus.protonsdk.di.ProtonModule
import com.metallicus.protonsdk.eosio.commander.ec.EosPrivateKey
import com.metallicus.protonsdk.model.*
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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

	fun getChainProvider(): LiveData<Resource<ChainProvider>> = liveData {
		emit(Resource.loading())

		try {
			val chainProvider = getChainProviderAsync()
			emit(Resource.success(chainProvider))
		} catch (e: ProtonException) {
			emit(Resource.error(e))
		} catch (e: Exception) {
			emit(Resource.error(e.localizedMessage.orEmpty()))
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

	fun getTokenContracts(): LiveData<Resource<List<TokenContract>>> = liveData {
		emit(Resource.loading())

		try {
			val tokenContracts = getTokenContractsAsync()
			emit(Resource.success(tokenContracts))
		} catch (e: ProtonException) {
			emit(Resource.error(e))
		} catch (e: Exception) {
			emit(Resource.error(e.localizedMessage.orEmpty()))
		}
	}

	fun generatePrivateKey(): EosPrivateKey {
		return EosPrivateKey()
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
			emit(Resource.error(e))
		} catch (e: Exception) {
			emit(Resource.error(e.localizedMessage.orEmpty()))
		}
	}

	fun setActiveAccount(activeAccount: ActiveAccount): LiveData<Resource<ChainAccount>> = liveData {
		emit(Resource.loading())

		try {
			val chainProvider = getChainProviderAsync()
			emit(accountModule.setActiveAccount(chainProvider.chainId, chainProvider.chainUrl, activeAccount))
		} catch (e: ProtonException) {
			emit(Resource.error(e))
		} catch (e: Exception) {
			emit(Resource.error(e.localizedMessage.orEmpty()))
		}
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
			emit(Resource.error(e))
		} catch (e: Exception) {
			emit(Resource.error(e.localizedMessage.orEmpty()))
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
			emit(Resource.error(e))
		} catch (e: Exception) {
			emit(Resource.error(e.localizedMessage.orEmpty()))
		}
	}

	fun getActiveAccountTokenBalances(): LiveData<Resource<List<TokenCurrencyBalance>>> = liveData {
		emit(Resource.loading())

		try {
			val tokenContracts = getTokenContractsAsync()
			val activeAccount = getActiveAccountAsync()

			val tokenContractsMap = mutableMapOf<String, String>()
			tokenContracts.forEach {
				tokenContractsMap["${it.contract}:${it.getSymbol()}"] = it.id
			}

			val exchangeRateUrl =
				activeAccount.chainProvider.chainUrl + activeAccount.chainProvider.exchangeRatePath

			tokenContractsModule.updateExchangeRates(exchangeRateUrl, tokenContractsMap)

			val tokenBalances =
				currencyBalancesModule.getTokenCurrencyBalances(
					activeAccount.chainProvider.hyperionHistoryUrl,
					activeAccount.account.accountName,
					tokenContractsMap)

			emit(tokenBalances)
		} catch (e: ProtonException) {
			emit(Resource.error(e))
		} catch (e: Exception) {
			emit(Resource.error(e.localizedMessage.orEmpty()))
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
			emit(Resource.error(e))
		} catch (e: Exception) {
			emit(Resource.error(e.localizedMessage.orEmpty()))
		}
	}

	fun getActiveAccountActions(contract: String, symbol: String): LiveData<Resource<List<Action>>> = liveData {
		emit(Resource.loading())

		try {
			val activeAccount = getActiveAccountAsync()

			val actions =
				actionsModule.getActions(
					activeAccount.chainProvider.chainUrl,
					activeAccount.chainProvider.hyperionHistoryUrl,
					activeAccount.account.accountName,
					contract,
					symbol)

			emit(actions)
		} catch (e: ProtonException) {
			emit(Resource.error(e))
		} catch (e: Exception) {
			emit(Resource.error(e.localizedMessage.orEmpty()))
		}
	}

	fun updateAccountName(pin: String, name: String): LiveData<Resource<ChainAccount>> = liveData {
		emit(Resource.loading())

		try {
			val activeAccount = getActiveAccountAsync()

			emit(accountModule.updateAccountName(activeAccount, pin, name))
		} catch (e: ProtonException) {
			emit(Resource.error(e))
		} catch (e: Exception) {
			emit(Resource.error(e.localizedMessage.orEmpty()))
		}
	}

	fun updateAccountAvatar(pin: String, byteArray: ByteArray): LiveData<Resource<ChainAccount>> = liveData {
		emit(Resource.loading())

		try {
			val activeAccount = getActiveAccountAsync()

			emit(accountModule.updateAccountAvatar(activeAccount, pin, byteArray))
		} catch (e: ProtonException) {
			emit(Resource.error(e))
		} catch (e: Exception) {
			emit(Resource.error(e.localizedMessage.orEmpty()))
		}
	}

	fun transferTokens(pin: String, contract: String, toAccount: String, amount: String, memo: String): LiveData<Resource<JsonObject>> = liveData {
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
			emit(Resource.error(e))
		} catch (e: Exception) {
			emit(Resource.error(e.localizedMessage.orEmpty()))
		}
	}
}