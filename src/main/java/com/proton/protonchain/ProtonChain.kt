package com.proton.protonchain

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.proton.protonchain.di.DaggerInjector
import com.proton.protonchain.di.ProtonModule
import com.proton.protonchain.model.ChainProvider
import com.proton.protonchain.model.Resource
import com.proton.protonchain.securestorage.PRNGFixes
import timber.log.Timber

class ProtonChain {
	companion object {
		fun initialize(context: Context) {
			// https://android-developers.googleblog.com/2013/08/some-securerandom-thoughts.html
			PRNGFixes.apply()

			if (BuildConfig.DEBUG) {
				Timber.plant(Timber.DebugTree())
			}

			DaggerInjector.buildComponent(context)
			DaggerInjector.component.inject(ProtonModule())
		}

		fun getChainProviders(lifeCycleOwner: LifecycleOwner, observer: Observer<Resource<List<ChainProvider>>>) {
			val chainProviderModule = ChainProviderModule()
			chainProviderModule.chainProviders.observe(lifeCycleOwner, observer)
			chainProviderModule.getChainProviders()
		}
	}
}