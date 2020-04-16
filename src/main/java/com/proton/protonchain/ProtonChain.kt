package com.proton.protonchain

import android.app.Application
import com.proton.protonchain.di.DaggerAppComponent
import com.proton.protonchain.securestorage.PRNGFixes
import timber.log.Timber

class ProtonChain {
	fun init(application: Application) {
		// https://android-developers.googleblog.com/2013/08/some-securerandom-thoughts.html
		PRNGFixes.apply()

		if (BuildConfig.DEBUG) {
			Timber.plant(Timber.DebugTree())
		}

		DaggerAppComponent.builder().application(application)
			.build().inject(application)
	}
}