package com.proton.protonchain

import android.content.Context
import androidx.lifecycle.*
import com.proton.protonchain.common.SingletonHolder
import com.proton.protonchain.di.DaggerInjector
import com.proton.protonchain.di.ProtonModule
import com.proton.protonchain.model.ChainProvider
import com.proton.protonchain.model.Resource
import com.proton.protonchain.model.TokenContract
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

    fun createAccount() {

	}

	fun importAccount(privateKey: String) {

	}

	fun getAccount() {

	}

	fun getAccountTokens(chainId: String, accountName: String) {

	}
}