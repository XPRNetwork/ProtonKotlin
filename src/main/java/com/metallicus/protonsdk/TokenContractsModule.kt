package com.metallicus.protonsdk

import android.content.Context
import com.metallicus.protonsdk.di.DaggerInjector
import com.metallicus.protonsdk.model.TokenContract
import com.metallicus.protonsdk.repository.TokenContractRepository
import javax.inject.Inject

class TokenContractsModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var tokenContractRepository: TokenContractRepository

	init {
		DaggerInjector.component.inject(this)
	}

	suspend fun getTokenContracts(chainId: String): List<TokenContract> {
		return tokenContractRepository.getAllTokenContracts(chainId)
	}
}