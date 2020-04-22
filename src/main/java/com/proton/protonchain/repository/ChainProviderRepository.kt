package com.proton.protonchain.repository

import com.google.gson.JsonObject
import com.proton.protonchain.api.ProtonChainService
import com.proton.protonchain.db.ChainProviderDao
import com.proton.protonchain.model.ChainProvider
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChainProviderRepository @Inject constructor(
	private val chainProviderDao: ChainProviderDao,
	private val protonChainService: ProtonChainService
) {
	fun removeAll() {
		chainProviderDao.removeAll()
	}

	fun addChainProvider(chainProvider: ChainProvider) {
		chainProviderDao.insert(chainProvider)
	}

	suspend fun fetchChainProviders(chainProvidersUrl: String): Response<JsonObject> {
		return protonChainService.getChainProviders(chainProvidersUrl)
	}

	suspend fun getAllChainProviders(): List<ChainProvider> {
		return chainProviderDao.findAll()
	}

	suspend fun getChainProvider(id: String): ChainProvider {
		return chainProviderDao.findById(id)
	}
}
