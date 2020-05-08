package com.proton.protonchain

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.proton.protonchain.common.SingletonHolder
import com.proton.protonchain.di.DaggerInjector
import com.proton.protonchain.di.ProtonModule
import com.proton.protonchain.model.ChainProvider
import com.proton.protonchain.model.Resource
import com.proton.protonchain.model.TokenContract
import com.proton.protonchain.securestorage.PRNGFixes
import timber.log.Timber

class ProtonChain private constructor(context: Context) {
	init {
		// https://android-developers.googleblog.com/2013/08/some-securerandom-thoughts.html
		PRNGFixes.apply()

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

	fun initialize() {
		workersModule.init()
	}

	fun getChainProviders(lifeCycleOwner: LifecycleOwner, observer: Observer<Resource<List<ChainProvider>>>) {
		chainProvidersModule.chainProviders.observe(lifeCycleOwner, observer)

		chainProvidersModule.chainProviders.postValue(Resource.loading(null))

		workersModule.onInitChainProviders(lifeCycleOwner, { success ->
			if (success) {
				chainProvidersModule.getChainProviders()
			} else {
				chainProvidersModule.chainProviders.postValue(Resource.error("Initialization Error", null))
			}
		})
	}

	fun getTokenContracts(lifeCycleOwner: LifecycleOwner, observer: Observer<Resource<List<TokenContract>>>) {
		tokenContractsModule.tokenContracts.observe(lifeCycleOwner, observer)

		tokenContractsModule.tokenContracts.postValue(Resource.loading(null))

		workersModule.onInitTokenContracts(lifeCycleOwner, { success ->
			if (success) {
				tokenContractsModule.getTokenContracts(defaultProtonChainId)
			} else {
				tokenContractsModule.tokenContracts.postValue(Resource.error("Initialization Error", null))
			}
		})
	}
}