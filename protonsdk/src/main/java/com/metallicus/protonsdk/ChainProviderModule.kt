package com.metallicus.protonsdk

import android.content.Context
import com.metallicus.protonsdk.common.Prefs
import com.metallicus.protonsdk.di.DaggerInjector
import com.metallicus.protonsdk.model.ChainProvider
import com.metallicus.protonsdk.repository.ChainProviderRepository
import javax.inject.Inject

class ChainProviderModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var chainProviderRepository: ChainProviderRepository

	@Inject
	lateinit var prefs: Prefs

	init {
		DaggerInjector.component.inject(this)
	}

	suspend fun getActiveChainProvider(): ChainProvider {
		val chainId = prefs.activeChainId
		return chainProviderRepository.getChainProvider(chainId)
	}
}