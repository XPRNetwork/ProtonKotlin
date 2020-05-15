package com.proton.protonchain

import android.content.Context
import com.proton.protonchain.di.DaggerInjector
import com.proton.protonchain.model.TokenContract
import com.proton.protonchain.repository.TokenContractRepository
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