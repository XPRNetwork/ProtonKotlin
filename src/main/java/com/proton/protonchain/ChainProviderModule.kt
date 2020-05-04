package com.proton.protonchain

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.proton.protonchain.di.DaggerInjector
import com.proton.protonchain.model.ChainProvider
import com.proton.protonchain.model.Resource
import com.proton.protonchain.repository.ChainProviderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ChainProviderModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var chainProviderRepository: ChainProviderRepository

	private var chainProvidersUrl: String

	init {
		DaggerInjector.component.inject(this)
		chainProvidersUrl = context.getString(R.string.chainProvidersUrl)
	}

	suspend fun fetchChainProviders(): Resource<List<ChainProvider>> {
		return try {
			val response = chainProviderRepository.fetchChainProviders(chainProvidersUrl)
			if (response.isSuccessful) {
				val gson = Gson()
				val chainProviders = mutableListOf<ChainProvider>()
				response.body()?.entrySet()?.forEach { entry ->
					val type = object : TypeToken<Map<String, Any>>() {}.type
					val chainProviderMap = gson.fromJson<Map<String, Any>>(entry.value, type)
					val chainProvider = ChainProvider(chainProviderMap)

					chainProviderRepository.addChainProvider(chainProvider)

					chainProviders.add(chainProvider)
				}

				Resource.success(chainProviders)
			} else {
				val msg = response.errorBody()?.string()
				val errorMsg = if (msg.isNullOrEmpty()) {
					response.message()
				} else {
					msg
				}
				Resource.error(errorMsg, null)
			}
		} catch (e: Exception) {
			Timber.d(e)

			Resource.error(e.localizedMessage.orEmpty(), null)
		}
	}

	val chainProviders = MutableLiveData<Resource<List<ChainProvider>>>()
	fun getChainProviders(shouldFetch: Boolean = true) = GlobalScope.launch {
		chainProviders.postValue(Resource.loading(null))

		chainProviders.postValue(withContext(Dispatchers.Default) {
			if (shouldFetch) {
				fetchChainProviders()
			} else {
				val chainProviders = chainProviderRepository.getAllChainProviders()
				Resource.success(chainProviders)
			}
		})
	}
}