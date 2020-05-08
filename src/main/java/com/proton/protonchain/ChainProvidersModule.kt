package com.proton.protonchain

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.proton.protonchain.di.DaggerInjector
import com.proton.protonchain.model.ChainProvider
import com.proton.protonchain.model.Resource
import com.proton.protonchain.repository.ChainProviderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

	val chainProviders = MutableLiveData<Resource<List<ChainProvider>>>()
	fun getChainProviders() = GlobalScope.launch {
		chainProviders.postValue(Resource.loading(null))

		chainProviders.postValue(withContext(Dispatchers.Default) {
			val chainProviders = chainProviderRepository.getAllChainProviders()
			Resource.success(chainProviders)
		})
	}
}