package com.metallicus.protonsdk

import android.content.Context
import androidx.lifecycle.*
import com.metallicus.protonsdk.common.Resource
import com.metallicus.protonsdk.common.SingletonHolder
import com.metallicus.protonsdk.di.DaggerInjector
import com.metallicus.protonsdk.di.ProtonModule
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

	private val protonCoroutineScope = CoroutineScope(Dispatchers.Default)

	fun initialize(chainProviderUrl: String, apiKey: String="", apiSecret: String="") {
		workersModule.init(chainProviderUrl, apiKey, apiSecret)
	}

	private suspend fun getChainProviderAsync() = suspendCoroutine<ChainProvider> { continuation ->
		workersModule.onInitChainProvider { success ->
			if (success) {
				protonCoroutineScope.launch {
					continuation.resume(chainProviderModule.getActiveChainProvider())
				}
			} else {
				continuation.resumeWithException(Exception("Initialization Error: No Chain Provider"))
			}
		}
	}

	fun getChainProvider(): LiveData<Resource<ChainProvider>> = liveData {
		emit(Resource.loading())

		try {
			val chainProvider = getChainProviderAsync()
			emit(Resource.success(chainProvider))
		} catch (e: Exception) {
			emit(Resource.error(e.localizedMessage.orEmpty(), null))
		}
	}

	private suspend fun getTokenContractsAsync() = suspendCoroutine<List<TokenContract>> { continuation ->
		workersModule.onInitTokenContracts { success ->
			if (success) {
				protonCoroutineScope.launch {
					continuation.resume(tokenContractsModule.getTokenContracts())
				}
			} else {
				continuation.resumeWithException(Exception("Initialization Error"))
			}
		}
	}

	fun getTokenContracts(): LiveData<Resource<List<TokenContract>>> = liveData {
		emit(Resource.loading())

		try {
			val tokenContracts = getTokenContractsAsync()
			emit(Resource.success(tokenContracts))
		} catch (e: Exception) {
			emit(Resource.error(e.localizedMessage.orEmpty(), null))
		}
	}

	fun hasActiveAccount(): Boolean {
		return accountModule.hasActiveAccount()
	}

	fun findAccounts(privateKeyStr: String): LiveData<Resource<List<ChainAccount>>> = liveData {
		emit(Resource.loading())

		try {
			val chainProvider = getChainProviderAsync()

			emit(
				accountModule.getAccountsForPrivateKey(
					chainProvider.chainId,
					chainProvider.chainUrl,
					chainProvider.stateHistoryUrl,
					privateKeyStr))
		} catch (e: Exception) {
			emit(Resource.error(e.localizedMessage.orEmpty(), null))
		}
	}

	fun setActiveAccount(account: ChainAccount, privateKey: String, pin: String) {
		accountModule.setActiveAccount(account, privateKey, pin)
	}

	private suspend fun getActiveAccountAsync() = suspendCoroutine<ChainAccount> { continuation ->
		workersModule.onInitActiveAccount { success ->
			if (success) {
				protonCoroutineScope.launch {
					continuation.resume(accountModule.getActiveAccount())
				}
			} else {
				continuation.resumeWithException(Exception("Initialization Error"))
			}
		}
	}

	fun getActiveAccount(): LiveData<Resource<ChainAccount>> = liveData {
		emit(Resource.loading())

		try {
			val activeAccount = getActiveAccountAsync()
			emit(Resource.success(activeAccount))
		} catch (e: Exception) {
			emit(Resource.error(e.localizedMessage.orEmpty(), null))
		}
	}

	fun refreshActiveAccount(): LiveData<Resource<ChainAccount>> = liveData {
		emit(Resource.loading())

		emit(accountModule.refreshActiveAccount())
	}

	fun getActiveAccountTokenBalances(): LiveData<Resource<TokenCurrencyBalance>> = liveData {
		emit(Resource.loading())

	}
}