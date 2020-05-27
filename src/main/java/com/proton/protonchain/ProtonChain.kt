package com.proton.protonchain

import android.content.Context
import androidx.lifecycle.*
import com.proton.protonchain.common.SingletonHolder
import com.proton.protonchain.di.DaggerInjector
import com.proton.protonchain.di.ProtonModule
import com.proton.protonchain.eosio.commander.ec.EosPrivateKey
import com.proton.protonchain.model.*
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ProtonChain private constructor(context: Context) {
	init {
		if (BuildConfig.DEBUG) {
			Timber.plant(Timber.DebugTree())
		}

		DaggerInjector.buildComponent(context)
		DaggerInjector.component.inject(ProtonModule())
	}

	companion object : SingletonHolder<ProtonChain, Context>(::ProtonChain)

	private var workersModule: WorkersModule = WorkersModule()
	private var chainProvidersModule: ChainProvidersModule = ChainProvidersModule()
	private var tokenContractsModule: TokenContractsModule = TokenContractsModule()
	private var accountModule: AccountModule = AccountModule()

	private val defaultProtonChainId = context.getString(R.string.defaultProtonChainId)

	private val protonCoroutineScope = CoroutineScope(Dispatchers.Default)

	fun initialize() {
		workersModule.init()
	}

	fun getChainProviders(): LiveData<Resource<List<ChainProvider>>> = liveData {
		emit(Resource.loading())

		emit(suspendCoroutine<Resource<List<ChainProvider>>> { continuation ->
			workersModule.onInitChainProviders { success ->
				if (success) {
					protonCoroutineScope.launch {
						continuation.resume(Resource.success(chainProvidersModule.getChainProviders()))
					}
				} else {
					continuation.resume(Resource.error("Initialization Error", emptyList<ChainProvider>()))
				}
			}
		})
	}

	fun getTokenContracts(): LiveData<Resource<List<TokenContract>>> {
		return getTokenContracts(defaultProtonChainId)
	}

	fun getTokenContracts(chainId: String): LiveData<Resource<List<TokenContract>>> = liveData {
		emit(Resource.loading())

		emit(suspendCoroutine<Resource<List<TokenContract>>> { continuation ->
			workersModule.onInitTokenContracts { success ->
				if (success) {
					protonCoroutineScope.launch {
						continuation.resume(Resource.success(tokenContractsModule.getTokenContracts(chainId)))
					}
				} else {
					continuation.resume(Resource.error("Initialization Error", emptyList<TokenContract>()))
				}
			}
		})
	}

	fun findAccounts(privateKeyStr: String): LiveData<Resource<List<SelectableAccount>>> = liveData {
		emit(Resource.loading())

		val selectableAccounts = mutableListOf<SelectableAccount>()

		val chainProviders = chainProvidersModule.getChainProviders()
		if (chainProviders.isNotEmpty()) {
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
		} else {
			emit(Resource.error("No Chain Providers, needs initialization", selectableAccounts))
		}

		emit(Resource.success(selectableAccounts.toList()))
	}

	fun selectAccount(selectableAccount: SelectableAccount): LiveData<Resource<Account>> = liveData {
		emit(Resource.loading())

		TODO("need to implement")
	}

	fun getSelectedAccount(): LiveData<Resource<Account>> = liveData {
		TODO("need to implement")
	}

	fun getSelectedAccountTokens() {
		TODO("need to implement")
	}
}