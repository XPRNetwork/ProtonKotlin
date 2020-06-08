package com.metallicus.protonsdk

import android.content.Context
import com.metallicus.protonsdk.di.DaggerInjector
import com.metallicus.protonsdk.model.ChainProvider
import com.metallicus.protonsdk.repository.ChainProviderRepository
import javax.inject.Inject

class ChainProvidersModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var chainProviderRepository: ChainProviderRepository

	init {
		DaggerInjector.component.inject(this)
	}

	suspend fun getChainProviders(): List<ChainProvider> {
		return chainProviderRepository.getAllChainProviders()
	}
}