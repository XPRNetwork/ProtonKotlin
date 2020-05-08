package com.proton.protonchain

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.proton.protonchain.di.DaggerInjector
import com.proton.protonchain.model.Resource
import com.proton.protonchain.model.TokenContract
import com.proton.protonchain.repository.TokenContractRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TokenContractsModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var tokenContractRepository: TokenContractRepository

	init {
		DaggerInjector.component.inject(this)
	}

	val tokenContracts = MutableLiveData<Resource<List<TokenContract>>>()
	fun getTokenContracts(chainId: String) = GlobalScope.launch {
		tokenContracts.postValue(Resource.loading(null))

		tokenContracts.postValue(withContext(Dispatchers.Default) {
			val chainProviders = tokenContractRepository.getAllTokenContracts(chainId)
			Resource.success(chainProviders)
		})
	}
}