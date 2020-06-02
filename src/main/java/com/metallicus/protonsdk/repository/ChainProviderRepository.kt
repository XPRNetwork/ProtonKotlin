package com.metallicus.protonsdk.repository

import com.google.gson.JsonObject
import com.metallicus.protonsdk.api.ProtonChainService
import com.metallicus.protonsdk.db.ChainProviderDao
import com.metallicus.protonsdk.model.ChainProvider
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
