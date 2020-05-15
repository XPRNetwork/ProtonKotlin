package com.proton.protonchain

import android.content.Context
import com.proton.protonchain.di.DaggerInjector
import com.proton.protonchain.model.ChainProvider
import com.proton.protonchain.repository.ChainProviderRepository
import javax.inject.Inject

class ChainProvidersModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var chainProviderRepository: ChainProviderRepository

	private var chainProvidersUrl: String

	init {
		DaggerInjector.component.inject(this)
		chainProvidersUrl = context.getString(R.string.chainProvidersUrl)
	}

	suspend fun getChainProviders(): List<ChainProvider> {
		return chainProviderRepository.getAllChainProviders()
	}
}