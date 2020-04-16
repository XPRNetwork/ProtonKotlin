package com.proton.protonchain.repository

import com.proton.protonchain.db.ChainProviderDao
import com.proton.protonchain.model.ChainProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChainProviderRepository @Inject constructor(
	private val chainProviderDao: ChainProviderDao
) {
	fun removeAll() {
		chainProviderDao.removeAll()
	}

	fun addChainProvider(chainProvider: ChainProvider) {
		chainProviderDao.insert(chainProvider)
	}

	suspend fun getAllChainProviders(): List<ChainProvider> {
		return chainProviderDao.findAll()
	}

	suspend fun getChainProvider(id: String): ChainProvider {
		return chainProviderDao.findById(id)
	}
}
