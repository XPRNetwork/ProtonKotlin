package com.proton.protonchain

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.proton.protonchain.common.SingletonHolder
import com.proton.protonchain.di.DaggerInjector
import com.proton.protonchain.di.ProtonModule
import com.proton.protonchain.model.ChainProvider
import com.proton.protonchain.model.Resource
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
	private var chainProviderModule: ChainProviderModule = ChainProviderModule()

	fun initialize() {
		workersModule.prune()

		workersModule.init()
	}

	fun getChainProviders(lifeCycleOwner: LifecycleOwner, observer: Observer<Resource<List<ChainProvider>>>) {
		chainProviderModule.chainProviders.observe(lifeCycleOwner, observer)

		workersModule.onInitChainProviders(lifeCycleOwner, { success ->
			if (success) {
				chainProviderModule.getChainProviders(false)
			} else {
				chainProviderModule.getChainProviders()
			}
		})
	}
}