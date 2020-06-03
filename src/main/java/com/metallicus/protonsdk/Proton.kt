package com.metallicus.protonsdk

import android.content.Context
import androidx.lifecycle.*
import com.metallicus.protonsdk.common.Resource
import com.metallicus.protonsdk.common.SingletonHolder
import com.metallicus.protonsdk.di.DaggerInjector
import com.metallicus.protonsdk.di.ProtonModule
import com.metallicus.protonsdk.eosio.commander.ec.EosPrivateKey
import com.metallicus.protonsdk.model.*
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.resume
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
	private var chainProvidersModule: ChainProvidersModule = ChainProvidersModule()
	private var tokenContractsModule: TokenContractsModule = TokenContractsModule()
	private var accountModule: AccountModule = AccountModule()

	private val defaultProtonChainId = context.getString(R.string.defaultProtonChainId)

	private val protonCoroutineScope = CoroutineScope(Dispatchers.Default)

	fun initialize() {
		workersModule.init()
	}

	private suspend fun getChainProvidersAsync() = suspendCoroutine<List<ChainProvider>> { continuation ->
		workersModule.onInitChainProviders { success ->
			if (success) {
				protonCoroutineScope.launch {
					continuation.resume(chainProvidersModule.getChainProviders())
				}
			} else {
				continuation.resume(emptyList())
			}
		}
	}

	fun getChainProviders(): LiveData<Resource<List<ChainProvider>>> = liveData {
		emit(Resource.loading())
		val chainProviders = getChainProvidersAsync()
		if (chainProviders.isNotEmpty()) {
			emit(Resource.success(chainProviders))
		} else {
			emit(Resource.error("Initialization Error", chainProviders))
		}
	}

	private suspend fun getTokenContractsAsync(chainId: String) = suspendCoroutine<List<TokenContract>> { continuation ->
		workersModule.onInitTokenContracts { success ->
			if (success) {
				protonCoroutineScope.launch {
					continuation.resume(tokenContractsModule.getTokenContracts(chainId))
				}
			} else {
				continuation.resume(emptyList())
			}
		}
	}

	fun getTokenContracts(): LiveData<Resource<List<TokenContract>>> {
		return getTokenContracts(defaultProtonChainId)
	}

	fun getTokenContracts(chainId: String): LiveData<Resource<List<TokenContract>>> = liveData {
		emit(Resource.loading())

		val tokenContracts = getTokenContractsAsync(chainId)
		if (tokenContracts.isNotEmpty()) {
			emit(Resource.success(tokenContracts))
		} else {
			emit(Resource.error("Initialization Error", tokenContracts))
		}
	}

	fun findAccounts(privateKeyStr: String): LiveData<Resource<List<SelectableAccount>>> = liveData {
		emit(Resource.loading())

		val chainProviders = getChainProvidersAsync()
		if (chainProviders.isNotEmpty()) {
			val selectableAccounts = mutableListOf<SelectableAccount>()

			chainProviders.forEach { chainProvider ->
				val privateKey = EosPrivateKey(privateKeyStr)
				val publicKey = privateKey.publicKey.toString()
				if (publicKey.isNotEmpty()) {
					val accountNames =
						accountModule.getAccountNamesForKey(chainProvider.chainUrl, publicKey)
					accountNames.forEach { accountName ->
						val selectableAccount = SelectableAccount(
							privateKey = privateKey,
							accountName = accountName,
							chainProvider = chainProvider
						)
						selectableAccounts.add(selectableAccount)
					}
				}
			}

			emit(Resource.success(selectableAccounts.toList()))
		} else {
			emit(Resource.error("Initialization Error", emptyList<SelectableAccount>()))
		}
	}

	fun selectAccount(selectableAccount: SelectableAccount, pin: String): LiveData<Resource<ChainAccount>> = liveData {
		emit(Resource.loading())

		emit(accountModule.setSelectedAccount(selectableAccount, pin))
	}

	fun getSelectedAccount(): LiveData<Resource<ChainAccount>> = liveData {
		emit(Resource.loading())

		emit(accountModule.getSelectedAccount())
	}
}